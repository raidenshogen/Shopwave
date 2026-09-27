CREATE TABLE product_images (
                                id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                product_id    UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                                image_url     VARCHAR(500) NOT NULL,
                                alt_text      VARCHAR(255),
                                is_primary    BOOLEAN NOT NULL DEFAULT FALSE,
                                display_order INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_product_images_product_id
    ON product_images(product_id);