ALTER TABLE foods
    ADD COLUMN owner_id UUID;

-- Todo alimento que ya exista pertenece al usuario demo Favian.
UPDATE foods
SET owner_id = '11111111-1111-1111-1111-111111111111'
WHERE owner_id IS NULL;

ALTER TABLE foods
    ALTER COLUMN owner_id SET NOT NULL;

ALTER TABLE foods
    ADD CONSTRAINT fk_foods_owner
        FOREIGN KEY (owner_id)
            REFERENCES users(id);

CREATE INDEX idx_foods_owner_id
    ON foods(owner_id);