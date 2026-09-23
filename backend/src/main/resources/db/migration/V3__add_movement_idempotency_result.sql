ALTER TABLE food_movements
    ADD COLUMN expected_version BIGINT,
    ADD COLUMN remaining_quantity_after NUMERIC(12, 3),
    ADD COLUMN food_version_after BIGINT;

ALTER TABLE food_movements
    ADD CONSTRAINT ck_food_movements_expected_version_nonnegative
        CHECK (expected_version IS NULL OR expected_version >= 0),
    ADD CONSTRAINT ck_food_movements_remaining_after_nonnegative
        CHECK (remaining_quantity_after IS NULL OR remaining_quantity_after >= 0),
    ADD CONSTRAINT ck_food_movements_food_version_after_nonnegative
        CHECK (food_version_after IS NULL OR food_version_after >= 0),
    ADD CONSTRAINT ck_food_movements_idempotency_snapshot_complete
        CHECK (
            (expected_version IS NULL
                AND remaining_quantity_after IS NULL
                AND food_version_after IS NULL)
            OR
            (expected_version IS NOT NULL
                AND remaining_quantity_after IS NOT NULL
                AND food_version_after IS NOT NULL)
        );
