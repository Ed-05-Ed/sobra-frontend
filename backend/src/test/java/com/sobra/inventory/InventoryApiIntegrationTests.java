package com.sobra.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.sobra.inventory.repository.FoodMovementRepository;
import com.sobra.inventory.repository.FoodRepository;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(InventoryApiIntegrationTests.FixedClockConfiguration.class)
class InventoryApiIntegrationTests {

    private static final UUID MILK_ID = UUID.fromString("10000000-0000-4000-8000-000000000001");
    private static final UUID BANANA_ID = UUID.fromString("10000000-0000-4000-8000-000000000003");
    private static final LocalDate TODAY = LocalDate.of(2026, 1, 15);
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Mexico_City");

    @Container
    static final GenericContainer<?> POSTGRES = new GenericContainer<>(DockerImageName.parse("postgres:16"))
            .withEnv("POSTGRES_DB", "sobra_test")
            .withEnv("POSTGRES_USER", "sobra_test")
            .withEnv("POSTGRES_PASSWORD", "sobra_test_password")
            .withExposedPorts(5432)
            .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\s", 2))
            .withStartupTimeout(Duration.ofSeconds(90));

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://%s:%d/sobra_test"
                .formatted(POSTGRES.getHost(), POSTGRES.getMappedPort(5432)));
        registry.add("spring.datasource.username", () -> "sobra_test");
        registry.add("spring.datasource.password", () -> "sobra_test_password");
        registry.add("sobra.business-zone", BUSINESS_ZONE::getId);
    }

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FoodMovementRepository movementRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private Flyway flyway;

    private HttpClient httpClient;

    @BeforeEach
    void cleanIsolatedDatabase() {
        movementRepository.deleteAllInBatch();
        foodRepository.deleteAllInBatch();
        httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    @Test
    void listsSeededIngredientsAndAllMigrations() throws Exception {
        HttpResponse<String> first = get("/api/ingredients");
        HttpResponse<String> second = get("/api/ingredients");

        assertThat(first.statusCode()).isEqualTo(200);
        assertThat(first.body()).isEqualTo(second.body());
        assertThat(json(first).size()).isEqualTo(10);
        List<String> ingredientNames = new ArrayList<>();
        json(first).forEach(ingredient -> ingredientNames.add(ingredient.get("name").stringValue()));
        assertThat(ingredientNames)
                .containsExactly("Arroz", "Avena", "Cebolla", "Huevo", "Leche",
                        "Manzana", "Pan", "Plátano", "Tomate", "Yogur");
        assertThat(Arrays.stream(flyway.info().applied())
                .map(info -> info.getVersion().getVersion())
                .toList()).containsExactly("1", "2", "3", "4", "5", "6", "7", "8");
    }

    @Test
    void createsAndReadsAnActiveFoodWithNormalizedName() throws Exception {
        HttpResponse<String> creation = createFood(MILK_ID, "  Leche abierta  ", "1000", TODAY.plusDays(4));
        JsonNode created = json(creation);
        String foodId = created.get("id").stringValue();

        assertThat(creation.statusCode()).isEqualTo(201);
        assertThat(creation.headers().firstValue("Location")).hasValueSatisfying(
                value -> assertThat(value).endsWith("/api/foods/" + foodId));
        assertThat(created.get("name").stringValue()).isEqualTo("Leche abierta");
        assertThat(created.get("unit").stringValue()).isEqualTo("ML");
        assertThat(created.get("version").asLong()).isZero();

        HttpResponse<String> resource = get("/api/foods/" + foodId);
        HttpResponse<String> inventory = get("/api/foods");
        assertThat(resource.statusCode()).isEqualTo(200);
        assertThat(json(resource).get("id").stringValue()).isEqualTo(foodId);
        assertThat(json(inventory).size()).isEqualTo(1);
    }

    @Test
    void rejectsFractionalPieceQuantity() throws Exception {
        HttpResponse<String> response = createFood(BANANA_ID, "Plátanos", "1.5", TODAY.plusDays(2));

        assertError(response, 400, "VALIDATION_ERROR");
        assertThat(foodRepository.count()).isZero();
    }

    @Test
    void rejectsQuantityBeyondNumericPrecisionOrScale() throws Exception {
        HttpResponse<String> tooManyIntegers = createFood(MILK_ID, "Leche", "1000000000", TODAY);
        HttpResponse<String> tooManyDecimals = createFood(MILK_ID, "Leche", "0.0001", TODAY);

        assertError(tooManyIntegers, 400, "VALIDATION_ERROR");
        assertError(tooManyDecimals, 400, "VALIDATION_ERROR");
        assertThat(foodRepository.count()).isZero();
    }

    @Test
    void calculatesDateBoundariesWithTheInjectedClock() throws Exception {
        createFood(MILK_ID, "Ayer", "1", TODAY.minusDays(1));
        createFood(MILK_ID, "Hoy", "1", TODAY);
        createFood(MILK_ID, "Tres días", "1", TODAY.plusDays(3));
        createFood(MILK_ID, "Cuatro días", "1", TODAY.plusDays(4));

        JsonNode foods = json(get("/api/foods"));
        assertDate(foods.get(0), -1, "DATE_PASSED");
        assertDate(foods.get(1), 0, "PRIORITY");
        assertDate(foods.get(2), 3, "PRIORITY");
        assertDate(foods.get(3), 4, "UPCOMING");
    }

    @Test
    void updatesOnlyEditableMetadataAndRejectsEmptyOrNullPatch() throws Exception {
        JsonNode created = json(createFood(MILK_ID, "Leche", "1000", TODAY.plusDays(4)));
        String foodId = created.get("id").stringValue();

        HttpResponse<String> updatedResponse = patch("/api/foods/" + foodId, Map.of(
                "version", 0,
                "name", "  Leche fría  ",
                "labelDate", TODAY.plusDays(2),
                "dateType", "BEST_BEFORE"
        ));
        JsonNode updated = json(updatedResponse);
        assertThat(updatedResponse.statusCode()).isEqualTo(200);
        assertThat(updated.get("name").stringValue()).isEqualTo("Leche fría");
        assertThat(updated.get("remainingQuantity").decimalValue()).isEqualByComparingTo("1000");
        assertThat(updated.get("version").asLong()).isEqualTo(1);

        assertError(patch("/api/foods/" + foodId, Map.of("version", 1)),
                400, "VALIDATION_ERROR");
        assertError(sendJson(httpClient, "PATCH", "/api/foods/" + foodId,
                "{\"version\":1,\"name\":null}"), 400, "VALIDATION_ERROR");
        assertError(patch("/api/foods/" + foodId, Map.of("version", 1, "quantity", 5)),
                400, "MALFORMED_REQUEST");
        assertError(patch("/api/foods/" + foodId, Map.of("version", 0, "name", "Otra")),
                409, "STALE_VERSION");
    }

    @Test
    void appliesPartialConsumptionAndPersistsItsOutcome() throws Exception {
        JsonNode food = json(createFood(MILK_ID, "Leche", "1000", TODAY.plusDays(2)));
        String foodId = food.get("id").stringValue();
        UUID operationId = UUID.randomUUID();

        HttpResponse<String> response = movement(foodId, operationId, "CONSUMED", "500", 0);
        JsonNode movement = json(response);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(movement.get("operationId").stringValue()).isEqualTo(operationId.toString());
        assertThat(movement.get("remainingQuantityAfter").decimalValue()).isEqualByComparingTo("500");
        assertThat(movement.get("foodVersionAfter").asLong()).isEqualTo(1);
        assertThat(movement.get("wasPriorityAtConsumption").asBoolean()).isTrue();
        assertThat(movement.get("occurredAt").stringValue()).isEqualTo("2026-01-15T18:00:00Z");
        assertThat(movementRepository.count()).isEqualTo(1);
        assertThat(json(get("/api/foods/" + foodId)).get("remainingQuantity").decimalValue())
                .isEqualByComparingTo("500");
    }

    @Test
    void keepsDepletedLotsVisible() throws Exception {
        JsonNode food = json(createFood(BANANA_ID, "Plátano", "1", TODAY.plusDays(2)));
        String foodId = food.get("id").stringValue();

        assertThat(movement(foodId, UUID.randomUUID(), "CONSUMED", "1", 0).statusCode()).isEqualTo(200);

        JsonNode inventory = json(get("/api/foods"));
        assertThat(inventory.size()).isEqualTo(1);
        assertThat(inventory.get(0).get("id").stringValue()).isEqualTo(foodId);
        assertThat(inventory.get(0).get("remainingQuantity").decimalValue()).isEqualByComparingTo("0");
    }

    @Test
    void rejectsInsufficientBalanceWithoutPartialChanges() throws Exception {
        JsonNode food = json(createFood(MILK_ID, "Leche", "100", TODAY.plusDays(2)));
        String foodId = food.get("id").stringValue();

        HttpResponse<String> response = movement(foodId, UUID.randomUUID(), "CONSUMED", "101", 0);

        assertError(response, 409, "INSUFFICIENT_QUANTITY");
        JsonNode unchanged = json(get("/api/foods/" + foodId));
        assertThat(unchanged.get("remainingQuantity").decimalValue()).isEqualByComparingTo("100");
        assertThat(unchanged.get("version").asLong()).isZero();
        assertThat(movementRepository.count()).isZero();
    }

    @Test
    void alwaysMarksWasteAsNotPriority() throws Exception {
        JsonNode food = json(createFood(MILK_ID, "Leche", "100", TODAY.plusDays(1)));
        String foodId = food.get("id").stringValue();

        JsonNode movement = json(movement(foodId, UUID.randomUUID(), "WASTED", "25", 0));

        assertThat(movement.get("wasPriorityAtConsumption").asBoolean()).isFalse();
        assertThat(movement.get("remainingQuantityAfter").decimalValue()).isEqualByComparingTo("75");
    }

    @Test
    void replaysOriginalResultAfterAnotherMovement() throws Exception {
        JsonNode food = json(createFood(MILK_ID, "Leche", "1000", TODAY.plusDays(2)));
        String foodId = food.get("id").stringValue();
        UUID firstOperation = UUID.randomUUID();

        HttpResponse<String> first = movement(foodId, firstOperation, "CONSUMED", "100", 0);
        movement(foodId, UUID.randomUUID(), "WASTED", "100", 1);
        HttpResponse<String> replay = movement(foodId, firstOperation, "CONSUMED", "100.000", 0);

        assertThat(replay.statusCode()).isEqualTo(200);
        assertThat(json(replay).get("movementId").stringValue())
                .isEqualTo(json(first).get("movementId").stringValue());
        assertThat(json(replay).get("remainingQuantityAfter").decimalValue()).isEqualByComparingTo("900");
        assertThat(json(replay).get("foodVersionAfter").asLong()).isEqualTo(1);
        assertThat(json(get("/api/foods/" + foodId)).get("remainingQuantity").decimalValue())
                .isEqualByComparingTo("800");
        assertThat(movementRepository.count()).isEqualTo(2);
    }

    @Test
    void rejectsReuseOfOperationIdWithDifferentContent() throws Exception {
        JsonNode food = json(createFood(MILK_ID, "Leche", "1000", TODAY.plusDays(2)));
        String foodId = food.get("id").stringValue();
        UUID operationId = UUID.randomUUID();

        movement(foodId, operationId, "CONSUMED", "100", 0);
        HttpResponse<String> conflict = movement(foodId, operationId, "CONSUMED", "101", 0);

        assertError(conflict, 409, "IDEMPOTENCY_CONFLICT");
        assertThat(movementRepository.count()).isEqualTo(1);
        assertThat(json(get("/api/foods/" + foodId)).get("remainingQuantity").decimalValue())
                .isEqualByComparingTo("900");
    }

    @Test
    void concurrentConsumptionsWithSameVersionApplyOnlyOnce() throws Exception {
        JsonNode food = json(createFood(MILK_ID, "Leche", "1000", TODAY.plusDays(2)));
        String foodId = food.get("id").stringValue();
        String firstBody = movementJson(UUID.randomUUID(), "CONSUMED", "100", 0);
        String secondBody = movementJson(UUID.randomUUID(), "CONSUMED", "100", 0);

        List<HttpResponse<String>> responses = concurrentPosts(
                "/api/foods/" + foodId + "/movements", firstBody, secondBody);

        assertThat(responses).extracting(HttpResponse::statusCode).containsExactlyInAnyOrder(200, 409);
        HttpResponse<String> conflict = responses.stream()
                .filter(response -> response.statusCode() == 409)
                .findFirst()
                .orElseThrow();
        assertThat(json(conflict).get("code").stringValue()).isEqualTo("STALE_VERSION");
        assertThat(movementRepository.count()).isEqualTo(1);
        JsonNode current = json(get("/api/foods/" + foodId));
        assertThat(current.get("remainingQuantity").decimalValue()).isEqualByComparingTo("900");
        assertThat(current.get("version").asLong()).isEqualTo(1);
    }

    @Test
    void concurrentRequestsWithSameOperationHaveOneEffect() throws Exception {
        JsonNode food = json(createFood(MILK_ID, "Leche", "1000", TODAY.plusDays(2)));
        String foodId = food.get("id").stringValue();
        String body = movementJson(UUID.randomUUID(), "CONSUMED", "100", 0);

        List<HttpResponse<String>> responses = concurrentPosts(
                "/api/foods/" + foodId + "/movements", body, body);

        assertThat(responses).extracting(HttpResponse::statusCode).containsOnly(200);
        assertThat(json(responses.get(0)).get("movementId").stringValue())
                .isEqualTo(json(responses.get(1)).get("movementId").stringValue());
        assertThat(movementRepository.count()).isEqualTo(1);
        JsonNode current = json(get("/api/foods/" + foodId));
        assertThat(current.get("remainingQuantity").decimalValue()).isEqualByComparingTo("900");
        assertThat(current.get("version").asLong()).isEqualTo(1);
    }

    @Test
    void archivesFoodWithoutDeletingItsHistory() throws Exception {
        JsonNode food = json(createFood(MILK_ID, "Leche", "1000", TODAY.plusDays(2)));
        String foodId = food.get("id").stringValue();
        UUID operationId = UUID.randomUUID();
        JsonNode movement = json(movement(foodId, operationId, "CONSUMED", "100", 0));

        HttpResponse<String> deletion = delete("/api/foods/" + foodId + "?version="
                + movement.get("foodVersionAfter").asLong());

        assertThat(deletion.statusCode()).isEqualTo(204);
        assertError(get("/api/foods/" + foodId), 404, "FOOD_NOT_FOUND");
        assertThat(json(get("/api/foods")).size()).isZero();
        assertThat(foodRepository.findById(UUID.fromString(foodId))).get().extracting("archived").isEqualTo(true);
        assertThat(movementRepository.count()).isEqualTo(1);
        assertThat(movement(foodId, operationId, "CONSUMED", "100.000", 0).statusCode()).isEqualTo(200);
        assertError(movement(foodId, UUID.randomUUID(), "CONSUMED", "1", 2),
                409, "FOOD_ARCHIVED");
        assertError(delete("/api/foods/" + foodId + "?version=2"), 409, "FOOD_ARCHIVED");
    }

    @Test
    void malformedUuidAndEnumUseThePublicErrorFormat() throws Exception {
        assertError(get("/api/foods/not-a-uuid"), 400, "MALFORMED_REQUEST");

        JsonNode food = json(createFood(MILK_ID, "Leche", "100", TODAY));
        HttpResponse<String> invalidEnum = sendJson(httpClient, "POST",
                "/api/foods/" + food.get("id").stringValue() + "/movements",
                "{\"operationId\":\"" + UUID.randomUUID()
                        + "\",\"type\":\"DONATED\",\"quantity\":1,\"expectedVersion\":0}");
        assertError(invalidEnum, 400, "MALFORMED_REQUEST");
        assertThat(json(invalidEnum).get("details").isArray()).isTrue();
    }

    private HttpResponse<String> createFood(
            UUID ingredientId,
            String name,
            String quantity,
            LocalDate labelDate
    ) throws Exception {
        return post("/api/foods", Map.of(
                "ingredientId", ingredientId,
                "name", name,
                "quantity", new BigDecimal(quantity),
                "labelDate", labelDate,
                "dateType", "EXPIRATION"
        ));
    }

    private HttpResponse<String> movement(
            String foodId,
            UUID operationId,
            String type,
            String quantity,
            long expectedVersion
    ) throws Exception {
        return sendJson(httpClient, "POST", "/api/foods/" + foodId + "/movements",
                movementJson(operationId, type, quantity, expectedVersion));
    }

    private String movementJson(UUID operationId, String type, String quantity, long expectedVersion)
            throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "operationId", operationId,
                "type", type,
                "quantity", new BigDecimal(quantity),
                "expectedVersion", expectedVersion
        ));
    }

    private HttpResponse<String> get(String path) throws Exception {
        return sendJson(httpClient, "GET", path, null);
    }

    private HttpResponse<String> post(String path, Object body) throws Exception {
        return sendJson(httpClient, "POST", path, objectMapper.writeValueAsString(body));
    }

    private HttpResponse<String> patch(String path, Object body) throws Exception {
        return sendJson(httpClient, "PATCH", path, objectMapper.writeValueAsString(body));
    }

    private HttpResponse<String> delete(String path) throws Exception {
        return sendJson(httpClient, "DELETE", path, null);
    }

    private HttpResponse<String> sendJson(HttpClient client, String method, String path, String body)
            throws Exception {
        HttpRequest.BodyPublisher publisher = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .method(method, publisher)
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private List<HttpResponse<String>> concurrentPosts(String path, String firstBody, String secondBody)
            throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(3);
        try {
            Future<HttpResponse<String>> first = executor.submit(() -> {
                barrier.await(10, TimeUnit.SECONDS);
                return sendJson(HttpClient.newHttpClient(), "POST", path, firstBody);
            });
            Future<HttpResponse<String>> second = executor.submit(() -> {
                barrier.await(10, TimeUnit.SECONDS);
                return sendJson(HttpClient.newHttpClient(), "POST", path, secondBody);
            });
            barrier.await(10, TimeUnit.SECONDS);
            return List.of(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    private JsonNode json(HttpResponse<String> response) throws Exception {
        return objectMapper.readTree(response.body());
    }

    private void assertError(HttpResponse<String> response, int status, String code) throws Exception {
        assertThat(response.statusCode()).isEqualTo(status);
        JsonNode body = json(response);
        assertThat(body.get("code").stringValue()).isEqualTo(code);
        assertThat(body.get("message").stringValue()).isNotBlank();
        assertThat(body.get("details").isArray()).isTrue();
    }

    private void assertDate(JsonNode food, long days, String status) {
        assertThat(food.get("daysUntilLabelDate").asLong()).isEqualTo(days);
        assertThat(food.get("dateStatus").stringValue()).isEqualTo(status);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FixedClockConfiguration {

        @Bean
        @Primary
        Clock fixedBusinessClock() {
            return Clock.fixed(Instant.parse("2026-01-15T18:00:00Z"), BUSINESS_ZONE);
        }
    }
}
