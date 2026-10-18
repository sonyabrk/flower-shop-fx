DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS bouquets;
DROP TABLE IF EXISTS customers;

CREATE TABLE customers (
    id         BIGSERIAL PRIMARY KEY,
    full_name  VARCHAR(150) NOT NULL,
    phone      VARCHAR(30)  NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE bouquets (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    price       NUMERIC(10, 2) NOT NULL CHECK (price > 0)
);

CREATE TABLE orders (
    id             BIGSERIAL PRIMARY KEY,
    customer_id    BIGINT NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    bouquet_id     BIGINT NOT NULL REFERENCES bouquets(id) ON DELETE RESTRICT,
    quantity       INTEGER NOT NULL CHECK (quantity > 0),
    total_price    NUMERIC(10, 2) NOT NULL CHECK (total_price > 0),
    status         VARCHAR(20) NOT NULL
                   CHECK (status IN ('NEW', 'CONFIRMED', 'IN_DELIVERY', 'DELIVERED', 'CANCELLED')),
    order_date     DATE NOT NULL,
    delivery_date  DATE
);