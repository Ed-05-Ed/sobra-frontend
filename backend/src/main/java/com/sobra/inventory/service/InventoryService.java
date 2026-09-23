package com.sobra.inventory.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sobra.inventory.api.dto.CreateFoodRequest;
import com.sobra.inventory.api.dto.FoodResponse;
import com.sobra.inventory.api.dto.IngredientResponse;
import com.sobra.inventory.api.dto.UpdateFoodRequest;
import com.sobra.inventory.model.DateType;
import com.sobra.inventory.model.Food;
import com.sobra.inventory.model.Ingredient;
import com.sobra.inventory.repository.FoodRepository;
import com.sobra.inventory.repository.IngredientRepository;
import com.sobra.shared.exception.ApiException;
import com.sobra.shared.exception.ErrorCode;
import com.sobra.shared.exception.ErrorDetail;
import com.sobra.user.model.User;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.sobra.inventory.api.dto.ExpiringFoodResponse;
import com.sobra.inventory.model.ExpirationUrgency;
import com.sobra.user.repository.UserRepository;
import com.sobra.user.model.User;
@Service
public class InventoryService {

    private final IngredientRepository ingredientRepository;
    private final FoodRepository foodRepository;
    private final InventoryInputValidator inputValidator;
    private final FoodResponseMapper responseMapper;
    private final Clock clock;
    private final UserRepository userRepository;

    private static final UUID DEFAULT_USER_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    public InventoryService(
            IngredientRepository ingredientRepository,
            FoodRepository foodRepository,
            UserRepository userRepository,
            InventoryInputValidator inputValidator,
            FoodResponseMapper responseMapper,
            Clock clock
    ) {
        this.ingredientRepository = ingredientRepository;
        this.foodRepository = foodRepository;
        this.userRepository = userRepository;
        this.inputValidator = inputValidator;
        this.responseMapper = responseMapper;
        this.clock = clock;
    }
    @Transactional(readOnly = true)
    public List<ExpiringFoodResponse> listExpiringFoods(UUID userId, int days) {
        LocalDate today = LocalDate.now(clock);

        int effectiveDays = Math.max(1, Math.min(days, 30));

        LocalDate endDate = today.plusDays(effectiveDays);

        return foodRepository
                .findAllByOwnerIdAndArchivedFalseAndLabelDateBetweenOrderByLabelDateAsc(
                        userId,
                        today,
                        endDate
                )
                .stream()
                .map(food -> {
                    long daysRemaining =
                            ChronoUnit.DAYS.between(today, food.getLabelDate());

                    ExpirationUrgency urgency = determineUrgency(daysRemaining);

                    return new ExpiringFoodResponse(
                            food.getId(),
                            food.getName(),
                            food.getLabelDate(),
                            daysRemaining,
                            urgency,
                            recommendationFor(urgency)
                    );
                })
                .toList();
    }
    @Transactional(readOnly = true)
    public List<IngredientResponse> listIngredients() {
        return ingredientRepository.findAllByOrderByNameAsc().stream()
                .map(ingredient -> new IngredientResponse(
                        ingredient.getId(), ingredient.getName(), ingredient.getUnit()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FoodResponse> listFoods() {
        return foodRepository.findAllByArchivedFalseOrderByLabelDateAscIdAsc().stream()
                .map(responseMapper::toResponse)
                .toList();
    }
    @Transactional(readOnly = true)
    public List<FoodResponse> listFoods(UUID userId) {
        UUID effectiveUserId = userId != null ? userId : DEFAULT_USER_ID;

        return foodRepository
                .findAllByOwnerIdAndArchivedFalseOrderByLabelDateAscIdAsc(effectiveUserId)
                .stream()
                .map(responseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FoodResponse getFood(UUID foodId) {
        return responseMapper.toResponse(requireActiveFood(foodId));
    }

    @Transactional
    public FoodResponse createFood(CreateFoodRequest request) {
        UUID effectiveUserId =
                request.userId() != null ? request.userId() : DEFAULT_USER_ID;

        User owner = userRepository.findById(effectiveUserId)
                .orElseThrow(() -> ApiException.notFound(
                        ErrorCode.USER_NOT_FOUND,
                        "El usuario solicitado no existe."
                ));
        Ingredient ingredient = ingredientRepository.findById(request.ingredientId())
                .orElseThrow(() -> ApiException.notFound(ErrorCode.INGREDIENT_NOT_FOUND,
                        "El ingrediente solicitado no existe."));
        String normalizedName = inputValidator.normalizeName(request.name());
        inputValidator.validateQuantity(request.quantity(), ingredient.getUnit(), "quantity");
        Instant now = clock.instant();
        Food food = new Food(
                owner,
                ingredient,
                normalizedName,
                request.quantity(),
                request.labelDate(),
                request.dateType(),
                now
        );
        foodRepository.saveAndFlush(food);
        return responseMapper.toResponse(food);
    }

    @Transactional
    public FoodResponse updateFood(UUID foodId, UpdateFoodRequest request) {
        Food food = requireFood(foodId);
        rejectArchived(food);
        requireVersion(food, request.getVersion());
        if (!request.hasName() && !request.hasLabelDate() && !request.hasDateType()) {
            throw ApiException.badRequest("Incluye al menos un metadato editable.",
                    new ErrorDetail("request", "name, labelDate o dateType es obligatorio"));
        }

        String name = food.getName();
        if (request.hasName()) {
            name = inputValidator.normalizeName(request.getName());
        }
        java.time.LocalDate labelDate = food.getLabelDate();
        if (request.hasLabelDate()) {
            if (request.getLabelDate() == null) {
                throw explicitNull("labelDate");
            }
            labelDate = request.getLabelDate();
        }
        DateType dateType = food.getDateType();
        if (request.hasDateType()) {
            if (request.getDateType() == null) {
                throw explicitNull("dateType");
            }
            dateType = request.getDateType();
        }

        food.updateMetadata(name, labelDate, dateType, clock.instant());
        foodRepository.flush();
        return responseMapper.toResponse(food);
    }

    @Transactional
    public void archiveFood(UUID foodId, long expectedVersion) {
        Food food = requireFood(foodId);
        rejectArchived(food);
        requireVersion(food, expectedVersion);
        food.archive(clock.instant());
        foodRepository.flush();
    }

    private Food requireActiveFood(UUID foodId) {
        return foodRepository.findByIdAndArchivedFalse(foodId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.FOOD_NOT_FOUND,
                        "El alimento solicitado no existe."));
    }

    private Food requireFood(UUID foodId) {
        return foodRepository.findById(foodId)
                .orElseThrow(() -> ApiException.notFound(ErrorCode.FOOD_NOT_FOUND,
                        "El alimento solicitado no existe."));
    }

    private void rejectArchived(Food food) {
        if (food.isArchived()) {
            throw ApiException.conflict(ErrorCode.FOOD_ARCHIVED, "El alimento ya está archivado.");
        }
    }

    private void requireVersion(Food food, Long expectedVersion) {
        if (expectedVersion == null || expectedVersion < 0) {
            throw ApiException.badRequest("La versión es obligatoria y no puede ser negativa.",
                    new ErrorDetail("version", "debe ser mayor o igual a cero"));
        }
        if (food.getVersion() != expectedVersion) {
            throw ApiException.conflict(ErrorCode.STALE_VERSION,
                    "La versión enviada ya no corresponde al alimento.");
        }
    }

    private ApiException explicitNull(String field) {
        return ApiException.badRequest("No se admiten valores nulos explícitos.",
                new ErrorDetail(field, "no puede ser nulo cuando se envía"));
    }
    private ExpirationUrgency determineUrgency(long daysRemaining) {
        if (daysRemaining <= 0) {
            return ExpirationUrgency.URGENT;
        }

        if (daysRemaining <= 2) {
            return ExpirationUrgency.HIGH;
        }

        return ExpirationUrgency.MEDIUM;
    }
    private String recommendationFor(ExpirationUrgency urgency) {
        return switch (urgency) {
            case URGENT ->
                    "Consúmelo hoy o considera donarlo inmediatamente.";

            case HIGH ->
                    "Consúmelo pronto o considera publicarlo.";

            case MEDIUM ->
                    "Planea consumirlo en los próximos días.";
        };
    }

}
