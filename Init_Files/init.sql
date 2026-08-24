-- =========================================================
-- Single init script for the e-commerce microservices project
-- MariaDB version. Creates all 6 per-service databases, their
-- schemas, and sample seed data.
--
-- Run with:
--   mysql -u root -p < init.sql
-- or from inside the mysql client:
--   SOURCE init.sql;
--
-- Unlike psql's \c, MariaDB's `USE dbname;` just switches the
-- active database for the rest of the session — no reconnect
-- needed, so this all runs as one script in one client session.
--
-- Note on updated_at: instead of a trigger + function (the
-- Postgres approach), MariaDB can auto-update a timestamp column
-- natively with "ON UPDATE CURRENT_TIMESTAMP" in the column
-- definition, so no triggers are needed anywhere in this script.
-- =========================================================


-- ============ auth_db ============
CREATE DATABASE IF NOT EXISTS auth_db;
USE auth_db;

CREATE TABLE app_user (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(100)  NOT NULL UNIQUE,
    email           VARCHAR(255)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255)  NOT NULL,
    role            VARCHAR(20)   NOT NULL DEFAULT 'CUSTOMER'
                        CHECK (role IN ('CUSTOMER', 'ADMIN')),
    is_active       BOOLEAN       NOT NULL DEFAULT TRUE,
    email_verified  BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_app_user_email (email)
) ENGINE=InnoDB;

-- Refresh tokens, one row per issued token (supports logout/revocation)
CREATE TABLE refresh_token (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL,
    token_hash      VARCHAR(255)  NOT NULL,
    expires_at      DATETIME      NOT NULL,
    revoked         BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_refresh_token_user_id (user_id),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---- seed data: auth_db ----
-- All sample users share the password "Password123!"
-- (bcrypt hash below is a real hash of that string, cost factor 12)
INSERT INTO app_user (id, username, email, password_hash, role, is_active, email_verified) VALUES
(1, 'admin',     'admin@shopdemo.com',   '$2b$12$9RyTht7vw0jzymsSFdDwbO8QCm/cBrwN.59ix98rP5LkllZcUl3Ky', 'ADMIN',    TRUE, TRUE),
(2, 'sanket',    'sanket@shopdemo.com',  '$2b$12$9RyTht7vw0jzymsSFdDwbO8QCm/cBrwN.59ix98rP5LkllZcUl3Ky', 'CUSTOMER', TRUE, TRUE),
(3, 'priya',     'priya@shopdemo.com',   '$2b$12$9RyTht7vw0jzymsSFdDwbO8QCm/cBrwN.59ix98rP5LkllZcUl3Ky', 'CUSTOMER', TRUE, TRUE),
(4, 'rahul',     'rahul@shopdemo.com',   '$2b$12$9RyTht7vw0jzymsSFdDwbO8QCm/cBrwN.59ix98rP5LkllZcUl3Ky', 'CUSTOMER', TRUE, FALSE);

-- keep AUTO_INCREMENT in sync after explicit-id inserts
ALTER TABLE app_user AUTO_INCREMENT = 5;


-- ============ customer_db ============
-- user_id below references auth_db.app_user.id logically only —
-- no cross-database FK (MariaDB can't enforce FKs across databases
-- any more than Postgres can). Populated via a sync call or event
-- from auth-service at signup.
CREATE DATABASE IF NOT EXISTS customer_db;
USE customer_db;

CREATE TABLE customer (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL UNIQUE,  -- logical ref to auth_db.app_user.id
    full_name       VARCHAR(150) NOT NULL,
    phone           VARCHAR(20),
    address_line1   VARCHAR(255),
    address_line2   VARCHAR(255),
    city            VARCHAR(100),
    state           VARCHAR(100),
    postal_code     VARCHAR(20),
    country         VARCHAR(100),
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customer_user_id (user_id)
) ENGINE=InnoDB;

-- ---- seed data: customer_db ----
INSERT INTO customer (id, user_id, full_name, phone, address_line1, address_line2, city, state, postal_code, country) VALUES
(1, 2, 'Sanket Maske',  '9876500001', '221B Baker Colony', NULL,        'Pune',      'Maharashtra', '411001', 'India'),
(2, 3, 'Priya Sharma',  '9876500002', '45 MG Road',        'Flat 302',  'Bengaluru', 'Karnataka',   '560001', 'India'),
(3, 4, 'Rahul Verma',   '9876500003', '12 Sector 18',      NULL,        'Noida',     'Uttar Pradesh','201301','India');

ALTER TABLE customer AUTO_INCREMENT = 4;


-- ============ product_db ============
CREATE DATABASE IF NOT EXISTS product_db;
USE product_db;

CREATE TABLE product (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    sku             VARCHAR(50)   NOT NULL UNIQUE,
    name            VARCHAR(255)  NOT NULL,
    description     TEXT,
    category        VARCHAR(100),
    price           DECIMAL(12,2) NOT NULL CHECK (price >= 0),
    is_active       BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_category (category)
) ENGINE=InnoDB;

-- ---- seed data: product_db ----
INSERT INTO product (id, sku, name, description, category, price, is_active) VALUES
(1, 'ELEC-WM-001', 'Wireless Mouse',        'Ergonomic 2.4GHz wireless mouse',        'Electronics', 799.00,  TRUE),
(2, 'ELEC-KB-002', 'Mechanical Keyboard',   'Tactile-switch mechanical keyboard',     'Electronics', 3499.00, TRUE),
(3, 'ELEC-HP-003', 'Noise Cancelling Headphones', 'Over-ear ANC headphones',          'Electronics', 6999.00, TRUE),
(4, 'HOME-BT-004', 'Insulated Water Bottle', '1L stainless steel bottle',             'Home',        499.00,  TRUE),
(5, 'BOOK-SD-005', 'System Design Interview Book', 'Volume 1, paperback',             'Books',       899.00,  TRUE),
(6, 'ELEC-CH-006', '65W USB-C Charger',     'GaN fast charger, 2-port',               'Electronics', 1499.00, TRUE);

ALTER TABLE product AUTO_INCREMENT = 7;


-- ============ inventory_db ============
-- product_id is a logical ref to product_db.product.id — no cross-DB FK.
CREATE DATABASE IF NOT EXISTS inventory_db;
USE inventory_db;

CREATE TABLE inventory (
    id                INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    product_id        INT UNSIGNED NOT NULL UNIQUE,  -- logical ref to product_db.product.id
    available_qty     INT UNSIGNED NOT NULL DEFAULT 0,
    reserved_qty      INT UNSIGNED NOT NULL DEFAULT 0,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE inventory_ledger (
    id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    product_id        INT UNSIGNED NOT NULL,
    order_id          INT UNSIGNED NOT NULL,  -- logical ref to order_db.customer_order.id
    change_type       VARCHAR(20)  NOT NULL
                          CHECK (change_type IN ('RESERVE', 'RELEASE', 'COMMIT')),
    quantity          INT UNSIGNED NOT NULL,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_inventory_ledger_order_id (order_id)
) ENGINE=InnoDB;

-- ---- seed data: inventory_db ----
INSERT INTO inventory (id, product_id, available_qty, reserved_qty) VALUES
(1, 1, 150, 0),
(2, 2, 80,  5),
(3, 3, 40,  0),
(4, 4, 300, 0),
(5, 5, 60,  0),
(6, 6, 120, 3);

ALTER TABLE inventory AUTO_INCREMENT = 7;

INSERT INTO inventory_ledger (product_id, order_id, change_type, quantity) VALUES
(2, 101, 'RESERVE', 5),
(6, 102, 'RESERVE', 3);


-- ============ order_db ============
-- user_id / product_id are logical refs to auth_db / product_db —
-- no cross-DB FK. order_item -> customer_order is a real FK since
-- both tables live in this same database.
CREATE DATABASE IF NOT EXISTS order_db;
USE order_db;

CREATE TABLE customer_order (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL,   -- logical ref to auth_db.app_user.id
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'CONFIRMED', 'FAILED', 'CANCELLED')),
    total_amount    DECIMAL(12,2) NOT NULL CHECK (total_amount >= 0),
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customer_order_user_id (user_id)
) ENGINE=InnoDB;

CREATE TABLE order_item (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id        INT UNSIGNED  NOT NULL,
    product_id      INT UNSIGNED  NOT NULL,   -- logical ref to product_db.product.id
    quantity        SMALLINT UNSIGNED NOT NULL CHECK (quantity > 0),
    unit_price      DECIMAL(12,2) NOT NULL CHECK (unit_price >= 0),  -- price snapshot at order time
    INDEX idx_order_item_order_id (order_id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES customer_order(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Transactional outbox: written in the same DB transaction as the
-- order, then relayed to the message broker by a separate publisher
-- process/polling job.
CREATE TABLE outbox_event (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    aggregate_id    VARCHAR(50)   NOT NULL,   -- e.g. the order id
    event_type      VARCHAR(50)   NOT NULL,   -- e.g. OrderPlaced, OrderConfirmed, OrderFailed
    payload         JSON          NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED')),
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at    DATETIME,
    INDEX idx_outbox_event_status (status)
) ENGINE=InnoDB;

-- ---- seed data: order_db ----
INSERT INTO customer_order (id, user_id, status, total_amount) VALUES
(100, 2, 'CONFIRMED', 2097.00),  -- Sanket: mouse + water bottle, completed
(101, 3, 'PENDING',   17495.00), -- Priya: keyboard x5, stock reserved, payment in flight
(102, 4, 'PENDING',   4497.00),  -- Rahul: charger x3, stock reserved, payment in flight
(103, 2, 'FAILED',    6999.00);  -- Sanket: headphones, payment declined, stock released

ALTER TABLE customer_order AUTO_INCREMENT = 104;

INSERT INTO order_item (order_id, product_id, quantity, unit_price) VALUES
(100, 1, 2, 799.00),
(100, 4, 1, 499.00),
(101, 2, 5, 3499.00),
(102, 6, 3, 1499.00),
(103, 3, 1, 6999.00);

INSERT INTO outbox_event (aggregate_id, event_type, payload, status, published_at) VALUES
('100', 'OrderConfirmed', JSON_OBJECT('orderId', 100, 'userId', 2, 'totalAmount', 2097.00), 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 2 DAY)),
('103', 'OrderFailed',    JSON_OBJECT('orderId', 103, 'userId', 2, 'reason', 'PAYMENT_DECLINED'), 'PUBLISHED', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('101', 'OrderPlaced',    JSON_OBJECT('orderId', 101, 'userId', 3, 'totalAmount', 17495.00), 'PENDING', NULL);


-- ============ payment_db ============
-- order_id is a logical ref to order_db.customer_order.id — no cross-DB FK.
CREATE DATABASE IF NOT EXISTS payment_db;
USE payment_db;

CREATE TABLE payment (
    id                 INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id           INT UNSIGNED  NOT NULL,   -- logical ref to order_db.customer_order.id
    idempotency_key    VARCHAR(100)  NOT NULL UNIQUE,  -- prevents duplicate charges on retry
    amount             DECIMAL(12,2) NOT NULL CHECK (amount >= 0),
    currency           VARCHAR(3)    NOT NULL DEFAULT 'INR',
    payment_method     VARCHAR(30)   NOT NULL,   -- CARD, UPI, NETBANKING, etc.
    status             VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                           CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    transaction_ref    VARCHAR(100),              -- external gateway reference
    created_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_payment_order_id (order_id)
) ENGINE=InnoDB;

-- ---- seed data: payment_db ----
INSERT INTO payment (id, order_id, idempotency_key, amount, currency, payment_method, status, transaction_ref) VALUES
(1, 100, 'idem-order-100-attempt1', 2097.00,  'INR', 'UPI',  'SUCCESS', 'TXN-9F3A2C'),
(2, 103, 'idem-order-103-attempt1', 6999.00,  'INR', 'CARD', 'FAILED',  NULL);

ALTER TABLE payment AUTO_INCREMENT = 3;

-- orders 101 and 102 have no payment row yet — they're still PENDING,
-- mirroring the in-flight reservations seeded in inventory_db