CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE categories (
                            id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            name        VARCHAR(100) NOT NULL UNIQUE,
                            slug        VARCHAR(100) NOT NULL UNIQUE,
                            description TEXT,
                            image_url   VARCHAR(500),
                            parent_id   UUID REFERENCES categories(id),
                            is_active   BOOLEAN NOT NULL DEFAULT TRUE,
                            created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                            updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_categories_slug ON categories(slug);
CREATE INDEX idx_categories_parent_id ON categories(parent_id);