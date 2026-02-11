-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS logistics DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE logistics;

-- 订单表会由JPA自动创建，这里只是参考
-- CREATE TABLE IF NOT EXISTS orders (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     order_no VARCHAR(50) UNIQUE,
--     cargo_name VARCHAR(100),
--     cargo_type VARCHAR(50),
--     cargo_weight DOUBLE,
--     cargo_volume DOUBLE,
--     cargo_quantity INT,
--     remark VARCHAR(500),
--     express_company VARCHAR(50),
--     origin VARCHAR(255),
--     destination VARCHAR(255),
--     sender_name VARCHAR(50),
--     receiver_name VARCHAR(50),
--     sender_phone VARCHAR(20),
--     receiver_phone VARCHAR(20),
--     status VARCHAR(20),
--     create_time VARCHAR(50),
--     tracking_no VARCHAR(50),
--     duration INT,
--     track_points_json TEXT,
--     ship_time DATETIME,
--     receive_time DATETIME,
--     cancel_time DATETIME,
--     origin_lng DOUBLE,
--     origin_lat DOUBLE,
--     dest_lng DOUBLE,
--     dest_lat DOUBLE
-- );
