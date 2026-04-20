CREATE TABLE IF NOT EXISTS "items" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    description VARCHAR(255),
    img_pth VARCHAR(255),
    price BIGINT
);

CREATE TABLE IF NOT EXISTS "cart-item" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT,
    item_id BIGINT,
    count INT
);

CREATE TABLE IF NOT EXISTS "orders" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    total_sum BIGINT
);

CREATE TABLE IF NOT EXISTS "order-item" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    item_id BIGINT,
    count INT
);
