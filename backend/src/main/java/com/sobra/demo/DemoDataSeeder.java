package com.sobra.demo;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sobra.inventory.model.DateType;
import com.sobra.inventory.model.Food;
import com.sobra.inventory.model.Ingredient;
import com.sobra.inventory.repository.FoodRepository;
import com.sobra.inventory.repository.IngredientRepository;
import com.sobra.user.model.User;
import com.sobra.user.repository.UserRepository;

@Component
public class DemoDataSeeder implements ApplicationRunner {

    private static final UUID FAVIAN_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID ANA_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    private final FoodRepository foodRepository;
    private final IngredientRepository ingredientRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public DemoDataSeeder(
            FoodRepository foodRepository,
            IngredientRepository ingredientRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.foodRepository = foodRepository;
        this.ingredientRepository = ingredientRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        System.out.println(">>> DEMO SEEDER: inicio");

        User favian = userRepository.findById(FAVIAN_ID)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existe el usuario demo Favian"
                        )
                );

        User ana = userRepository.findById(ANA_ID)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existe el usuario demo Ana"
                        )
                );

        long favianFoods =
                foodRepository.countByOwnerIdAndArchivedFalse(FAVIAN_ID);

        long anaFoods =
                foodRepository.countByOwnerIdAndArchivedFalse(ANA_ID);

        System.out.println(
                ">>> DEMO SEEDER: alimentos actuales -> Favian="
                        + favianFoods
                        + ", Ana="
                        + anaFoods
        );

        if (favianFoods > 0 || anaFoods > 2) {
            System.out.println(
                    ">>> DEMO SEEDER: ya existen alimentos, no se insertan datos demo"
            );
            return;
        }

        LocalDate today = LocalDate.now(clock);
        Instant now = Instant.now(clock);

        System.out.println(">>> DEMO SEEDER: sembrando despensa...");

        seedFavian(favian, today, now);
        seedAna(ana, today, now);

        System.out.println(">>> DEMO SEEDER: despensa creada correctamente");
    }

    private void seedFavian(
            User owner,
            LocalDate today,
            Instant now
    ) {
        save(
                owner,
                ingredient("Leche"),
                "Leche deslactosada",
                "900",
                today.plusDays(1),
                DateType.EXPIRATION,
                now
        );

        save(
                owner,
                ingredient("Yogur"),
                "Yogur natural",
                "700",
                today.plusDays(2),
                DateType.EXPIRATION,
                now
        );

        save(
                owner,
                ingredient("Pan"),
                "Pan integral",
                "8",
                today,
                DateType.BEST_BEFORE,
                now
        );

        save(
                owner,
                ingredient("Huevo"),
                "Huevos",
                "12",
                today.plusDays(9),
                DateType.EXPIRATION,
                now
        );

        save(
                owner,
                ingredient("Plátano"),
                "Plátanos",
                "6",
                today.plusDays(3),
                DateType.BEST_BEFORE,
                now
        );

        save(
                owner,
                ingredient("Manzana"),
                "Manzanas rojas",
                "7",
                today.plusDays(12),
                DateType.BEST_BEFORE,
                now
        );

        save(
                owner,
                ingredient("Tomate"),
                "Tomates saladet",
                "5",
                today.plusDays(4),
                DateType.BEST_BEFORE,
                now
        );

        save(
                owner,
                ingredient("Cebolla"),
                "Cebollas blancas",
                "4",
                today.plusDays(18),
                DateType.BEST_BEFORE,
                now
        );

        save(
                owner,
                ingredient("Arroz"),
                "Arroz blanco",
                "1500",
                today.plusDays(120),
                DateType.BEST_BEFORE,
                now
        );

        save(
                owner,
                ingredient("Avena"),
                "Avena en hojuelas",
                "900",
                today.plusDays(80),
                DateType.BEST_BEFORE,
                now
        );

        save(
                owner,
                ingredient("Leche"),
                "Leche entera",
                "1000",
                today.plusDays(6),
                DateType.EXPIRATION,
                now
        );

        save(
                owner,
                ingredient("Yogur"),
                "Yogur de fresa",
                "450",
                today.plusDays(5),
                DateType.EXPIRATION,
                now
        );

        save(
                owner,
                ingredient("Pan"),
                "Pan para sándwich",
                "12",
                today.plusDays(4),
                DateType.BEST_BEFORE,
                now
        );

        save(
                owner,
                ingredient("Huevo"),
                "Huevos camperos",
                "6",
                today.plusDays(14),
                DateType.EXPIRATION,
                now
        );

        save(
                owner,
                ingredient("Tomate"),
                "Tomate cherry",
                "10",
                today.plusDays(7),
                DateType.BEST_BEFORE,
                now
        );
    }

    private void seedAna(
            User owner,
            LocalDate today,
            Instant now
    ) {
        save(
                owner,
                ingredient("Leche"),
                "Leche semidescremada",
                "750",
                today.plusDays(2),
                DateType.EXPIRATION,
                now
        );

        save(
                owner,
                ingredient("Yogur"),
                "Yogur griego",
                "500",
                today.plusDays(1),
                DateType.EXPIRATION,
                now
        );

        save(
                owner,
                ingredient("Pan"),
                "Bolillos",
                "5",
                today,
                DateType.BEST_BEFORE,
                now
        );

        save(
                owner,
                ingredient("Plátano"),
                "Plátanos maduros",
                "4",
                today.plusDays(2),
                DateType.BEST_BEFORE,
                now
        );

        save(
                owner,
                ingredient("Manzana"),
                "Manzanas verdes",
                "5",
                today.plusDays(10),
                DateType.BEST_BEFORE,
                now
        );

        save(
                owner,
                ingredient("Huevo"),
                "Cartón de huevos",
                "18",
                today.plusDays(11),
                DateType.EXPIRATION,
                now
        );

        save(
                owner,
                ingredient("Arroz"),
                "Arroz integral",
                "1000",
                today.plusDays(100),
                DateType.BEST_BEFORE,
                now
        );

        save(
                owner,
                ingredient("Avena"),
                "Avena tradicional",
                "600",
                today.plusDays(60),
                DateType.BEST_BEFORE,
                now
        );

        save(
                owner,
                ingredient("Tomate"),
                "Tomates",
                "6",
                today.plusDays(5),
                DateType.BEST_BEFORE,
                now
        );

        save(
                owner,
                ingredient("Cebolla"),
                "Cebollas moradas",
                "3",
                today.plusDays(16),
                DateType.BEST_BEFORE,
                now
        );
    }

    private void save(
            User owner,
            Ingredient ingredient,
            String name,
            String quantity,
            LocalDate labelDate,
            DateType dateType,
            Instant now
    ) {
        Food food = new Food(
                owner,
                ingredient,
                name,
                new BigDecimal(quantity),
                labelDate,
                dateType,
                now
        );

        foodRepository.save(food);
    }

    private Ingredient ingredient(String name) {
        return ingredientRepository.findByNameIgnoreCase(name)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No existe ingrediente demo: " + name
                        )
                );
    }
}