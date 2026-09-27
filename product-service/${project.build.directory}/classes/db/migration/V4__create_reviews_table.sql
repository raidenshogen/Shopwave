CREATE TABLE reviews (
                         id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         product_id  UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                         user_id     UUID NOT NULL,
                         rating      INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
                         title       VARCHAR(255),
                         comment     TEXT,
                         is_verified BOOLEAN NOT NULL DEFAULT FALSE,
                         created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reviews_product_id ON reviews(product_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE UNIQUE INDEX idx_reviews_product_user
    ON reviews(product_id, user_id);