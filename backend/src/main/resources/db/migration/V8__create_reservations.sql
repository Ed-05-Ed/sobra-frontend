CREATE TABLE reservations (
                              id UUID PRIMARY KEY,
                              listing_id UUID NOT NULL,
                              user_id UUID NOT NULL,
                              status VARCHAR(20) NOT NULL,
                              pickup_code VARCHAR(4) NOT NULL,
                              created_at TIMESTAMPTZ NOT NULL,

                              CONSTRAINT fk_reservations_listing
                                  FOREIGN KEY (listing_id)
                                      REFERENCES listings(id)
                                      ON DELETE RESTRICT,

                              CONSTRAINT fk_reservations_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES users(id)
                                      ON DELETE RESTRICT,

                              CONSTRAINT ck_reservations_status
                                  CHECK (status IN ('ACTIVE', 'CANCELLED', 'COMPLETED')),

                              CONSTRAINT ck_reservations_pickup_code
                                  CHECK (pickup_code ~ '^[0-9]{4}$')
);

CREATE INDEX idx_reservations_user
    ON reservations(user_id);

CREATE INDEX idx_reservations_listing
    ON reservations(listing_id);