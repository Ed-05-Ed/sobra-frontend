CREATE TABLE ingredients (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    unit VARCHAR(10) NOT NULL,
    CONSTRAINT uk_ingredients_name UNIQUE (name),
    CONSTRAINT ck_ingredients_unit CHECK (unit IN ('G', 'ML', 'PIECE'))
);

CREATE TABLE foods (
    id UUID PRIMARY KEY,
    ingredient_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    remaining_quantity NUMERIC(12, 3) NOT NULL,
    label_date DATE NOT NULL,
    date_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_foods_ingredient
        FOREIGN KEY (ingredient_id) REFERENCES ingredients (id) ON DELETE RESTRICT,
    CONSTRAINT ck_foods_remaining_quantity_nonnegative CHECK (remaining_quantity >= 0),
    CONSTRAINT ck_foods_date_type CHECK (date_type IN ('EXPIRATION', 'BEST_BEFORE'))
);

CREATE TABLE food_movements (
    id UUID PRIMARY KEY,
    operation_id UUID NOT NULL,
    food_id UUID NOT NULL,
    quantity NUMERIC(12, 3) NOT NULL,
    unit VARCHAR(10) NOT NULL,
    type VARCHAR(10) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    was_priority_at_consumption BOOLEAN NOT NULL,
    CONSTRAINT uk_food_movements_operation_id UNIQUE (operation_id),
    CONSTRAINT fk_food_movements_food
        FOREIGN KEY (food_id) REFERENCES foods (id) ON DELETE RESTRICT,
    CONSTRAINT ck_food_movements_quantity_positive CHECK (quantity > 0),
    CONSTRAINT ck_food_movements_unit CHECK (unit IN ('G', 'ML', 'PIECE')),
    CONSTRAINT ck_food_movements_type CHECK (type IN ('CONSUMED', 'WASTED')),
    CONSTRAINT ck_food_movements_piece_quantity_integer
        CHECK (unit <> 'PIECE' OR quantity = TRUNC(quantity))
);

CREATE INDEX idx_foods_active_label_date
    ON foods (label_date, id)
    WHERE archived = FALSE AND remaining_quantity > 0;

CREATE INDEX idx_foods_active_ingredient_label_date
    ON foods (ingredient_id, label_date, id)
    WHERE archived = FALSE AND remaining_quantity > 0;

CREATE INDEX idx_food_movements_food_occurred_at
    ON food_movements (food_id, occurred_at DESC);
