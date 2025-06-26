-- =========================
-- 1. USERS & AUTHENTICATION
-- =========================
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    phone VARCHAR(20),
    date_of_birth DATE,
    avatar TEXT,
    status VARCHAR(20) ,
    is_email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_addresses (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    is_default BOOLEAN DEFAULT FALSE,
    address_type VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_sessions (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    token TEXT NOT NULL,
    device_info TEXT,
    ip_address VARCHAR(45),
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



-- =========================
-- 2. SELLERS & STORES
-- =========================
CREATE TABLE sellers (
    id SERIAL PRIMARY KEY,
    user_id INT UNIQUE REFERENCES users(id),
    store_name VARCHAR(100) NOT NULL,
    store_description TEXT,
    store_logo TEXT,
    rating NUMERIC(3, 2) DEFAULT 0,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE seller_bank_accounts (
    id SERIAL PRIMARY KEY,
    seller_id INT REFERENCES sellers(id),
    bank_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    account_holder VARCHAR(100) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE seller_policies (
    id SERIAL PRIMARY KEY,
    seller_id INT REFERENCES sellers(id),
    policy_type VARCHAR(50) NOT NULL, -- return, shipping, etc.
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE store_contact_info (
    id SERIAL PRIMARY KEY,
    seller_id INT REFERENCES sellers(id),
    contact_type VARCHAR(50) NOT NULL, -- email, phone, website
    contact_value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE store_business_hours (
    id SERIAL PRIMARY KEY,
    seller_id INT REFERENCES sellers(id),
    day_of_week INT NOT NULL, -- 0 (Sunday) to 6 (Saturday)
    opening_time TIME,
    closing_time TIME,
    is_closed BOOLEAN DEFAULT FALSE
);

-- =========================
-- 3. PRODUCT CATALOG
-- =========================
CREATE TABLE brands (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    logo TEXT,
    description TEXT,
    website VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id INT REFERENCES categories(id),
    slug VARCHAR(255) UNIQUE,
    description TEXT,
    image TEXT,
    display_order INT DEFAULT 0
);

CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    seller_id INT REFERENCES sellers(id),
    category_id INT REFERENCES categories(id),
    brand_id INT REFERENCES brands(id),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    slug VARCHAR(255) UNIQUE,
    meta_title VARCHAR(255),
    meta_description TEXT,
    tax_rate NUMERIC(5, 2) DEFAULT 0,
    warranty_info TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE product_variants (
    id SERIAL PRIMARY KEY,
    product_id INT REFERENCES products(id),
    sku VARCHAR(100) UNIQUE NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    stock INT DEFAULT 0,
    attributes TEXT  -- optional for size, color etc.
);

CREATE TABLE product_images (
    id SERIAL PRIMARY KEY,
    product_id INT REFERENCES products(id),
    variant_id INT REFERENCES product_variants(id) NULL,
    image_url TEXT NOT NULL,
    alt_text VARCHAR(255),
    is_primary BOOLEAN DEFAULT FALSE,
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE product_attributes (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    attribute_type VARCHAR(50) NOT NULL -- color, size, material, etc.
);

CREATE TABLE product_attribute_values (
    id SERIAL PRIMARY KEY,
    attribute_id INT REFERENCES product_attributes(id),
    value VARCHAR(100) NOT NULL,
    display_value VARCHAR(100) NOT NULL
);



CREATE TABLE product_variant_attributes (
    variant_id INT REFERENCES product_variants(id),
    attribute_value_id INT REFERENCES product_attribute_values(id),
    PRIMARY KEY (variant_id, attribute_value_id)
);

CREATE TABLE product_tags (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE product_tag_relations (
    product_id INT REFERENCES products(id),
    tag_id INT REFERENCES product_tags(id),
    PRIMARY KEY (product_id, tag_id)
);

CREATE TABLE product_specifications (
    id SERIAL PRIMARY KEY,
    product_id INT REFERENCES products(id),
    spec_name VARCHAR(100) NOT NULL,
    spec_value TEXT NOT NULL,
    display_order INT DEFAULT 0
);

-- =========================
-- 4. ORDERS
-- =========================
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    total_amount NUMERIC(12, 2) NOT NULL,
    shipping_fee NUMERIC(12, 2) DEFAULT 0,
    tax_amount NUMERIC(12, 2) DEFAULT 0,
    discount_amount NUMERIC(12, 2) DEFAULT 0,
    coupon_code VARCHAR(50),
    status VARCHAR(20) DEFAULT 'pending',  -- pending, confirmed, shipped, delivered, canceled
    payment_method VARCHAR(50),
    shipping_address TEXT,
    tracking_number VARCHAR(100),
    shipping_carrier VARCHAR(100),
    estimated_delivery_date DATE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INT REFERENCES orders(id),
    product_variant_id INT REFERENCES product_variants(id),
    quantity INT NOT NULL,
    price NUMERIC(12, 2) NOT NULL
);

CREATE TABLE order_status_history (
    id SERIAL PRIMARY KEY,
    order_id INT REFERENCES orders(id),
    status VARCHAR(50) NOT NULL,
    comment TEXT,
    created_by INT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE returns (
    id SERIAL PRIMARY KEY,
    order_id INT REFERENCES orders(id),
    user_id INT REFERENCES users(id),
    return_reason TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'pending', -- pending, approved, rejected, completed
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE return_items (
    id SERIAL PRIMARY KEY,
    return_id INT REFERENCES returns(id),
    order_item_id INT REFERENCES order_items(id),
    quantity INT NOT NULL,
    condition_description TEXT,
    refund_amount NUMERIC(12, 2)
);

CREATE TABLE shipments (
    id SERIAL PRIMARY KEY,
    order_id INT REFERENCES orders(id),
    tracking_number VARCHAR(100),
    carrier VARCHAR(100),
    status VARCHAR(50) DEFAULT 'preparing', -- preparing, shipped, delivered
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shipment_items (
    id SERIAL PRIMARY KEY,
    shipment_id INT REFERENCES shipments(id),
    order_item_id INT REFERENCES order_items(id),
    quantity INT NOT NULL
);

-- =========================
-- 5. PAYMENTS & SELLER PAYOUTS
-- =========================
CREATE TABLE payment_methods (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    method_type VARCHAR(50) NOT NULL, -- credit_card, bank_transfer, etc.
    is_default BOOLEAN DEFAULT FALSE,
    details JSONB NOT NULL, -- store payment details in secure format
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    order_id INT REFERENCES orders(id),
    amount NUMERIC(12, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',  -- pending, completed, failed
    payment_method_details JSONB,
    transaction_id VARCHAR(255),
    refunded_amount NUMERIC(12, 2) DEFAULT 0,
    paid_at TIMESTAMP
);

CREATE TABLE seller_payouts (
    id SERIAL PRIMARY KEY,
    seller_id INT REFERENCES sellers(id),
    total_amount NUMERIC(12, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',  -- pending, paid, failed
    payout_date TIMESTAMP,
    notes TEXT
);

CREATE TABLE seller_transactions (
    id SERIAL PRIMARY KEY,
    seller_id INT REFERENCES sellers(id),
    order_id INT REFERENCES orders(id),
    amount NUMERIC(12, 2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- commission, refund, payout
    payout_id INT REFERENCES seller_payouts(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE invoices (
    id SERIAL PRIMARY KEY,
    order_id INT REFERENCES orders(id),
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    billing_address TEXT NOT NULL,
    tax_identification VARCHAR(50),
    issued_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP,
    status VARCHAR(20) DEFAULT 'unpaid' -- paid, unpaid, canceled
);

-- =========================
-- 6. INVENTORY & WAREHOUSE
-- =========================
CREATE TABLE warehouses (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location TEXT,
    manager_id INT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE warehouse_locations (
    id SERIAL PRIMARY KEY,
    warehouse_id INT REFERENCES warehouses(id),
    zone VARCHAR(50) NOT NULL,
    aisle VARCHAR(50) NOT NULL,
    shelf VARCHAR(50) NOT NULL,
    bin VARCHAR(50) NOT NULL,
    UNIQUE(warehouse_id, zone, aisle, shelf, bin)
);

CREATE TABLE product_location_mapping (
    id SERIAL PRIMARY KEY,
    warehouse_location_id INT REFERENCES warehouse_locations(id),
    product_variant_id INT REFERENCES product_variants(id),
    UNIQUE(warehouse_location_id, product_variant_id)
);

CREATE TABLE stock_levels (
    id SERIAL PRIMARY KEY,
    warehouse_id INT REFERENCES warehouses(id),
    product_variant_id INT REFERENCES product_variants(id),
    quantity INT NOT NULL DEFAULT 0,
    minimum_stock INT DEFAULT 0,
    reorder_point INT DEFAULT 0,
    UNIQUE(warehouse_id, product_variant_id)
);

CREATE TABLE stock_transactions (
    id SERIAL PRIMARY KEY,
    warehouse_id INT REFERENCES warehouses(id),
    product_variant_id INT REFERENCES product_variants(id),
    quantity INT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- import, export, return, adjust
    reference_id INT, -- optional
    created_by INT REFERENCES users(id),
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE damaged_inventory (
    id SERIAL PRIMARY KEY,
    warehouse_id INT REFERENCES warehouses(id),
    product_variant_id INT REFERENCES product_variants(id),
    quantity INT NOT NULL,
    reason TEXT,
    reported_by INT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inventory_alerts (
    id SERIAL PRIMARY KEY,
    product_variant_id INT REFERENCES product_variants(id),
    warehouse_id INT REFERENCES warehouses(id),
    alert_type VARCHAR(50) NOT NULL, -- low_stock, out_of_stock, expiring
    is_resolved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE product_batches (
    id SERIAL PRIMARY KEY,
    product_variant_id INT REFERENCES product_variants(id),
    warehouse_id INT REFERENCES warehouses(id),
    batch_number VARCHAR(100),
    manufacturing_date DATE,
    expiry_date DATE,
    quantity INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- 7. PURCHASE ORDERS
-- =========================
CREATE TABLE suppliers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    email VARCHAR(150),
    phone VARCHAR(20),
    address TEXT,
    tax_id VARCHAR(50),
    payment_terms VARCHAR(255),
    lead_time INT, -- in days
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE purchase_orders (
    id SERIAL PRIMARY KEY,
    supplier_id INT REFERENCES suppliers(id),
    supplier_name VARCHAR(100),
    total_cost NUMERIC(12,2),
    status VARCHAR(20) DEFAULT 'pending',
    created_by INT REFERENCES users(id),
    expected_delivery_date DATE,
    shipping_cost NUMERIC(12, 2) DEFAULT 0,
    received_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE purchase_order_items (
    id SERIAL PRIMARY KEY,
    purchase_order_id INT REFERENCES purchase_orders(id),
    product_variant_id INT REFERENCES product_variants(id),
    quantity INT,
    cost_price NUMERIC(12, 2)
);

CREATE TABLE purchase_order_status_history (
    id SERIAL PRIMARY KEY,
    purchase_order_id INT REFERENCES purchase_orders(id),
    status VARCHAR(50) NOT NULL,
    comment TEXT,
    created_by INT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE supplier_products (
    id SERIAL PRIMARY KEY,
    supplier_id INT REFERENCES suppliers(id),
    product_variant_id INT REFERENCES product_variants(id),
    supplier_product_code VARCHAR(100),
    supplier_price NUMERIC(12, 2),
    minimum_order_quantity INT DEFAULT 1,
    is_preferred BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- 8. REVIEWS & WISHLISTS
-- =========================
CREATE TABLE product_reviews (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    product_id INT REFERENCES products(id),
    title VARCHAR(255),
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    is_verified_purchase BOOLEAN DEFAULT FALSE,
    helpful_votes INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE review_responses (
    id SERIAL PRIMARY KEY,
    review_id INT REFERENCES product_reviews(id),
    user_id INT REFERENCES users(id),
    seller_id INT REFERENCES sellers(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE review_images (
    id SERIAL PRIMARY KEY,
    review_id INT REFERENCES product_reviews(id),
    image_url TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE review_votes (
    id SERIAL PRIMARY KEY,
    review_id INT REFERENCES product_reviews(id),
    user_id INT REFERENCES users(id),
    is_helpful BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(review_id, user_id)
);

CREATE TABLE wishlists (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    product_variant_id INT REFERENCES product_variants(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE compare_lists (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE compare_list_items (
    id SERIAL PRIMARY KEY,
    compare_list_id INT REFERENCES compare_lists(id),
    product_id INT REFERENCES products(id),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- 9. COUPONS & PROMOTIONS
-- =========================
CREATE TABLE coupons (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    discount_type VARCHAR(10) CHECK (discount_type IN ('percent', 'fixed')),
    discount_value NUMERIC(10, 2) NOT NULL,
    min_order_value NUMERIC(10, 2),
    expires_at TIMESTAMP,
    usage_limit INT,
    usage_count INT DEFAULT 0,
    specific_products BOOLEAN DEFAULT FALSE,
    specific_categories BOOLEAN DEFAULT FALSE,
    first_time_only BOOLEAN DEFAULT FALSE,
    minimum_items INT
);

CREATE TABLE coupon_usages (
    id SERIAL PRIMARY KEY,
    coupon_id INT REFERENCES coupons(id),
    user_id INT REFERENCES users(id),
    order_id INT REFERENCES orders(id),
    used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE coupon_products (
    coupon_id INT REFERENCES coupons(id),
    product_id INT REFERENCES products(id),
    PRIMARY KEY (coupon_id, product_id)
);

CREATE TABLE coupon_categories (
    coupon_id INT REFERENCES coupons(id),
    category_id INT REFERENCES categories(id),
    PRIMARY KEY (coupon_id, category_id)
);

CREATE TABLE promotions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    promotion_type VARCHAR(50) NOT NULL, -- flash_sale, bundle_deal, buy_x_get_y
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE promotion_products (
    promotion_id INT REFERENCES promotions(id),
    product_id INT REFERENCES products(id),
    discount_type VARCHAR(10) CHECK (discount_type IN ('percent', 'fixed')),
    discount_value NUMERIC(10, 2) NOT NULL,
    PRIMARY KEY (promotion_id, product_id)
);

CREATE TABLE bundle_deals (
    id SERIAL PRIMARY KEY,
    promotion_id INT REFERENCES promotions(id),
    name VARCHAR(100) NOT NULL,
    discount_type VARCHAR(10) CHECK (discount_type IN ('percent', 'fixed')),
    discount_value NUMERIC(10, 2) NOT NULL
);

CREATE TABLE bundle_items (
    bundle_id INT REFERENCES bundle_deals(id),
    product_id INT REFERENCES products(id),
    quantity INT DEFAULT 1,
    PRIMARY KEY (bundle_id, product_id)
);

CREATE TABLE loyalty_program (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    points_per_spend NUMERIC(10, 2),
    minimum_redemption_points INT,
    redemption_value NUMERIC(10, 2),
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE user_loyalty_points (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    points_balance INT DEFAULT 0,
    lifetime_points INT DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loyalty_point_transactions (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    points INT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- earn, redeem, expire, adjust
    order_id INT REFERENCES orders(id),
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- 10. STATISTICS & ANALYTICS
-- =========================
CREATE TABLE website_traffic (
    id SERIAL PRIMARY KEY,
    page_url VARCHAR(255) NOT NULL,
    user_id INT REFERENCES users(id),
    ip_address VARCHAR(45),
    user_agent TEXT,
    referrer_url TEXT,
    visit_duration INT, -- in seconds
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE product_views (
    id SERIAL PRIMARY KEY,
    product_id INT REFERENCES products(id),
    user_id INT REFERENCES users(id),
    ip_address VARCHAR(45),
    view_duration INT, -- in seconds
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shopping_carts (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    session_id VARCHAR(255), -- for guest users
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cart_items (
    id SERIAL PRIMARY KEY,
    cart_id INT REFERENCES shopping_carts(id),
    product_variant_id INT REFERENCES product_variants(id),
    quantity INT NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE abandoned_carts (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    session_id VARCHAR(255),
    reminder_sent_at TIMESTAMP,
    converted_to_order BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sales_stats (
    id SERIAL PRIMARY KEY,
    date DATE NOT NULL,
    product_id INT REFERENCES products(id),
    seller_id INT REFERENCES sellers(id),
    units_sold INT DEFAULT 0,
    revenue NUMERIC(12, 2) DEFAULT 0,
    discount_amount NUMERIC(12, 2) DEFAULT 0,
    UNIQUE(date, product_id)
);

-- =========================
-- 11. SYSTEM SETTINGS & CONTENT
-- =========================
CREATE TABLE system_settings (
    id SERIAL PRIMARY KEY,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT,
    setting_description TEXT,
    setting_group VARCHAR(50),
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE content_pages (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    content TEXT,
    meta_title VARCHAR(255),
    meta_description TEXT,
    is_published BOOLEAN DEFAULT TRUE,
    created_by INT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE menu_items (
    id SERIAL PRIMARY KEY,
    parent_id INT REFERENCES menu_items(id),
    title VARCHAR(100) NOT NULL,
    url VARCHAR(255),
    order_index INT DEFAULT 0,
    menu_location VARCHAR(50) NOT NULL, -- header, footer, sidebar
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE homepage_banners (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255),
    image_url TEXT NOT NULL,
    link_url TEXT,
    display_order INT DEFAULT 0,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- 12. NOTIFICATIONS & COMMUNICATIONS
-- =========================
CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    content TEXT,
    notification_type VARCHAR(50), -- order_status, promotion, system
    is_read BOOLEAN DEFAULT FALSE,
    related_entity_type VARCHAR(50), -- order, product, etc.
    related_entity_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE email_templates (
    id SERIAL PRIMARY KEY,
    template_name VARCHAR(100) UNIQUE NOT NULL,
    subject VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    variables TEXT, -- documentation of available variables
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE email_log (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    template_id INT REFERENCES email_templates(id),
    subject VARCHAR(255) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    body_content TEXT,
    status VARCHAR(20) DEFAULT 'pending', -- pending, sent, failed
    error_message TEXT,
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE contact_messages (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(20),
    subject VARCHAR(255),
    message TEXT NOT NULL,
    status VARCHAR(20) ,
    replied_by INT REFERENCES users(id),
    replied_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);