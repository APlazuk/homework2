CREATE TABLE products
(
    id       BIGINT AUTO_INCREMENT NOT NULL,
    category VARCHAR(255)          NULL,
    name     VARCHAR(255)          NULL,
    price    DECIMAL(38, 2)        NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);