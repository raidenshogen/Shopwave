CREATE TABLE addresses (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    label       VARCHAR(50),
    first_name  VARCHAR(100),
    last_name   VARCHAR(100),
    phone       VARCHAR(30),
    street      VARCHAR(255) NOT NULL,
    city        VARCHAR(100) NOT NULL,
    country     VARCHAR(100) NOT NULL,
    zip_code    VARCHAR(20)  NOT NULL,
    is_default  BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_addresses_user_id ON addresses(user_id);