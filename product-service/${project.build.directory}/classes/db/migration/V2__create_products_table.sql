CREATE TABLE products (
                          id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          name         VARCHAR(255) NOT NULL,
                          slug         VARCHAR(255) NOT NULL UNIQUE,
                          description  TEXT,
                          price        DECIMAL(10,2) NOT NULL,
                          sale_price   DECIMAL(10,2),
                          sku          VARCHAR(100) NOT NULL UNIQUE,
                          stock        INTEGER NOT NULL DEFAULT 0,
                          seller_id    UUID NOT NULL,
                          category_id  UUID REFERENCES categories(id),
                          brand        VARCHAR(100),
                          status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                          is_featured  BOOLEAN NOT NULL DEFAULT FALSE,
                          avg_rating   DECIMAL(3,2) DEFAULT 0,
                          review_count INTEGER DEFAULT 0,
                          created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
                          updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_slug ON products(slug);
CREATE INDEX idx_products_seller_id ON products(seller_id);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_status ON products(status);