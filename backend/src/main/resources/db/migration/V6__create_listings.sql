CREATE TABLE listings (
                          id UUID PRIMARY KEY,
                          food_id UUID NOT NULL,
                          owner_id UUID NOT NULL,

                          type VARCHAR(20) NOT NULL,
                          status VARCHAR(20) NOT NULL,

                          price NUMERIC(12, 2),
                          description VARCHAR(300),

                          created_at TIMESTAMPTZ NOT NULL,

                          CONSTRAINT fk_listings_food
                              FOREIGN KEY (food_id)
                                  REFERENCES foods(id)
                                  ON DELETE RESTRICT,

                          CONSTRAINT fk_listings_owner
                              FOREIGN KEY (owner_id)
                                  REFERENCES users(id)
                                  ON DELETE RESTRICT,

                          CONSTRAINT ck_listings_type
                              CHECK (type IN ('SALE', 'DONATION')),

                          CONSTRAINT ck_listings_status
                              CHECK (status IN ('ACTIVE', 'RESERVED', 'CLOSED')),

                          CONSTRAINT ck_listings_sale_price
                              CHECK (
                                  (type = 'SALE' AND price IS NOT NULL AND price > 0)
                                      OR
                                  (type = 'DONATION' AND price IS NULL)
                                  )
);

CREATE INDEX idx_listings_active
    ON listings(status, created_at DESC);

CREATE INDEX idx_listings_owner
    ON listings(owner_id);

CREATE INDEX idx_listings_food
    ON listings(food_id);