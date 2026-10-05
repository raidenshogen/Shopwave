CREATE EXTENSION IF NOT EXISTS "pgcrypto";
-- Orders table
    CREATE TABLE orders (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        order_number VARCHAR(50) UNIQUE NOT NULL,
                        customer_id UUID NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                        subtotal DECIMAL(10,2) NOT NULL,
                        discount DECIMAL(10,2) DEFAULT 0.00,
                        shipping_fee DECIMAL(10,2) DEFAULT 0.00,
                        tax DECIMAL(10,2) DEFAULT 0.00,
                        total DECIMAL(10,2) NOT NULL,
                        coupon_code VARCHAR(50),
                        notes TEXT,
                        shipping_address JSONB NOT NULL,
                        payment_method VARCHAR(30),
                        created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                        updated_at TIMESTAMP
);

-- Order Items table
CREATE TABLE order_items (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             product_id UUID NOT NULL,
                             product_name VARCHAR(255) NOT NULL,
                             product_image VARCHAR(500),
                             quantity INTEGER NOT NULL CHECK (quantity > 0),
                             unit_price DECIMAL(10,2) NOT NULL,
                             total_price DECIMAL(10,2) NOT NULL
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);

-- Order Status History table
CREATE TABLE order_status_history (
                                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                      order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                                      status VARCHAR(30) NOT NULL,
                                      comment TEXT,
                                      changed_by UUID,
                                      changed_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_status_history_order_id ON order_status_history(order_id);
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);