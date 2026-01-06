
-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS catering_system
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE catering_system;

-- 2. 如果需要，先清空旧数据（注意：会删除原有数据）
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS blanket_orders;
DROP TABLE IF EXISTS menu_items;
DROP TABLE IF EXISTS menus;
DROP TABLE IF EXISTS recipes;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS system_config;
SET FOREIGN_KEY_CHECKS = 1;

-- 3. 建表
-- 用户表
CREATE TABLE users (
                       id           INT AUTO_INCREMENT PRIMARY KEY,
                       name         VARCHAR(50)  NOT NULL,
                       login_name   VARCHAR(50)  NOT NULL UNIQUE,
                       password     VARCHAR(100) NOT NULL,
                       phone        VARCHAR(20)  DEFAULT NULL,
                       department   VARCHAR(100) DEFAULT NULL,
                       workstation  VARCHAR(100) DEFAULT NULL,
                       role         VARCHAR(50)  NOT NULL,
                       INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- 食谱表
CREATE TABLE recipes (
                         id         INT AUTO_INCREMENT PRIMARY KEY,
                         name       VARCHAR(100) NOT NULL,
                         category   VARCHAR(100) DEFAULT NULL,
                         image      VARCHAR(255) DEFAULT NULL,
                         unit       VARCHAR(20)  DEFAULT NULL,
                         price      DECIMAL(10,2) NOT NULL DEFAULT 0,
                         created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         INDEX idx_recipes_name (name),
                         INDEX idx_recipes_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食谱';

-- 菜单表
CREATE TABLE menus (
                       id         INT AUTO_INCREMENT PRIMARY KEY,
                       name       VARCHAR(100) NOT NULL,
                       date       DATE         NOT NULL,
                       active     TINYINT(1)   NOT NULL DEFAULT 0,
                       created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       INDEX idx_menus_date (date),
                       INDEX idx_menus_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日菜单';

-- 菜单项表
CREATE TABLE menu_items (
                            id         INT AUTO_INCREMENT PRIMARY KEY,
                            menu_id    INT          NOT NULL,
                            recipe_id  INT          DEFAULT NULL,
                            name       VARCHAR(100) NOT NULL,
                            image      VARCHAR(255) DEFAULT NULL,
                            unit       VARCHAR(20)  DEFAULT NULL,
                            price      DECIMAL(10,2) NOT NULL DEFAULT 0,
                            created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_menu_items_menu
                                FOREIGN KEY (menu_id) REFERENCES menus(id)
                                    ON DELETE CASCADE,
                            CONSTRAINT fk_menu_items_recipe
                                FOREIGN KEY (recipe_id) REFERENCES recipes(id)
                                    ON DELETE SET NULL,
                            INDEX idx_menu_items_menu (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单中的具体菜品';

-- 订单表
CREATE TABLE orders (
                        id           INT AUTO_INCREMENT PRIMARY KEY,
                        user_id      INT          NOT NULL,
                        user_name    VARCHAR(50)  NOT NULL,
                        phone        VARCHAR(20)  DEFAULT NULL,
                        work_location VARCHAR(100) DEFAULT NULL COMMENT '工位信息（创建订单时保存）',
                        order_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        meal_date    DATE         NOT NULL,
                        total_price  DECIMAL(10,2) NOT NULL DEFAULT 0,
                        created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_orders_user
                            FOREIGN KEY (user_id) REFERENCES users(id),
                        INDEX idx_orders_user_date (user_id, meal_date),
                        INDEX idx_orders_meal_date (meal_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工订单';

-- 订单明细表
CREATE TABLE order_items (
                             id           INT AUTO_INCREMENT PRIMARY KEY,
                             order_id     INT          NOT NULL,
                             menu_item_id INT          DEFAULT NULL,
                             name         VARCHAR(100) NOT NULL,
                             unit         VARCHAR(20)  DEFAULT NULL,
                             quantity     INT          NOT NULL DEFAULT 1,
                             price        DECIMAL(10,2) NOT NULL DEFAULT 0,
                             subtotal     DECIMAL(10,2) NOT NULL DEFAULT 0,
                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id) REFERENCES orders(id)
                                     ON DELETE CASCADE,
                             CONSTRAINT fk_order_items_menu_item
                                 FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
                                     ON DELETE SET NULL,
                             INDEX idx_order_items_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

-- 总括订单表
CREATE TABLE blanket_orders (
                                id             INT AUTO_INCREMENT PRIMARY KEY,
                                date           DATE         NOT NULL,
                                item_name      VARCHAR(100) NOT NULL,
                                unit           VARCHAR(20)  DEFAULT NULL,
                                total_quantity INT          NOT NULL DEFAULT 0,
                                price          DECIMAL(10,2) NOT NULL DEFAULT 0,
                                subtotal       DECIMAL(10,2) NOT NULL DEFAULT 0,
                                INDEX idx_blanket_orders_date (date),
                                INDEX idx_blanket_orders_item (item_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='总括订单（按菜品汇总）';

-- 系统配置表
CREATE TABLE system_config (
                               id                   INT PRIMARY KEY CHECK (id = 1),
                               order_deadline       VARCHAR(5) NOT NULL DEFAULT '09:00',
                               delivery_start_time  VARCHAR(5) NOT NULL DEFAULT '11:30'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统时间配置';

INSERT INTO system_config (id, order_deadline, delivery_start_time)
VALUES (1, '09:00', '11:30')
    ON DUPLICATE KEY UPDATE
                         order_deadline      = VALUES(order_deadline),
                         delivery_start_time = VALUES(delivery_start_time);

-- 4. 初始化用户 + 食谱 + 菜单 + 菜品 + 多日订单（完整示例数据）

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE blanket_orders;
TRUNCATE TABLE menu_items;
TRUNCATE TABLE menus;
TRUNCATE TABLE recipes;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- 用户
INSERT INTO users (name, login_name, password, phone, department, workstation, role) VALUES
                                                                                         ('系统管理员', 'admin',    '123456', '13800000000', '信息部', '总部-001', 'manager'),
                                                                                         ('厨房主管',   'kitchen',  '123456', '13800000001', '厨房',   '后厨-001', 'kitchen_chief'),
                                                                                         ('配送员',     'delivery', '123456', '13800000002', '配送部', '配送-001',  'delivery_staff'),
                                                                                         ('财务人员',   'finance',  '123456', '13800000003', '财务部', '财务-001',  'finance'),
                                                                                         ('张三',       'zhangsan', '123456', '13800000004', '技术部', '3楼301',    'employee'),
                                                                                         ('李四',       'lisi',     '123456', '13800000005', '市场部', '2楼201',    'employee'),
                                                                                         ('王五',       'wangwu',   '123456', '13800000006', '人事部', '4楼401',    'employee'),
                                                                                         ('赵六',       'zhaoliu',  '123456', '13800000007', '运营部', '5楼501',    'employee');

-- 食谱
INSERT INTO recipes (name, category, image, unit, price) VALUES
                                                             ('宫保鸡丁',       '热菜', '/uploads/gbjd.jpg', '份', 12.00),
                                                             ('扬州炒饭',       '主食', '/uploads/yzcf.jpg', '份',  8.00),
                                                             ('鱼香肉丝',       '热菜', '/uploads/yxrs.jpg', '份', 15.00),
                                                             ('麻婆豆腐',       '热菜', '/uploads/mpdf.jpg', '份', 10.00),
                                                             ('西红柿鸡蛋汤',   '汤类', '/uploads/xhsjdt.jpg', '份',  6.00),
                                                             ('红烧茄子',       '热菜', '/uploads/hsqz.jpg', '份', 11.00),
                                                             ('土豆丝',         '凉菜', '/uploads/tds.jpg', '份',  6.00),
                                                             ('米饭',           '主食', '/uploads/mf.jpg', '两',  0.50),
                                                             ('鸡腿饭',         '套餐', '/uploads/jtf.jpg', '份', 18.00),
                                                             ('牛肉面',         '主食', '/uploads/nrm.jpg', '碗', 16.00),
                                                             ('青菜豆腐汤',     '汤类', '/uploads/qcdft.jpg', '份',  5.00);

-- 多日菜单
INSERT INTO menus (name, date, active, created_at) VALUES
                                                       ('111',        DATE '2025-12-16', 1, '2025-12-15 08:00:00'),
                                                       ('222',        DATE '2025-12-17', 1, '2025-12-16 08:00:00'),
                                                       ('333',        DATE '2025-12-18', 1, '2025-12-17 08:00:00'),
                                                       ('上月套餐A',  DATE '2025-11-15', 1, '2025-11-14 08:00:00'),
                                                       ('上月套餐B',  DATE '2025-11-30', 1, '2025-11-29 08:00:00');

SELECT id INTO @menu_111   FROM menus WHERE name='111';
SELECT id INTO @menu_222   FROM menus WHERE name='222';
SELECT id INTO @menu_333   FROM menus WHERE name='333';
SELECT id INTO @menu_m11   FROM menus WHERE name='上月套餐A';
SELECT id INTO @menu_m12   FROM menus WHERE name='上月套餐B';

-- 挂菜品到菜单
INSERT INTO menu_items (menu_id, recipe_id, name, image, unit, price, created_at)
SELECT @menu_111, id, name, image, unit, price, '2025-12-15 09:00:00' FROM recipes;
INSERT INTO menu_items (menu_id, recipe_id, name, image, unit, price, created_at)
SELECT @menu_222, id, name, image, unit, price, '2025-12-16 09:00:00' FROM recipes;
INSERT INTO menu_items (menu_id, recipe_id, name, image, unit, price, created_at)
SELECT @menu_333, id, name, image, unit, price, '2025-12-17 09:00:00' FROM recipes;

INSERT INTO menu_items (menu_id, recipe_id, name, image, unit, price, created_at)
SELECT @menu_m11, id, name, image, unit, price, '2025-11-14 09:00:00'
FROM recipes WHERE name IN ('鸡腿饭','青菜豆腐汤','米饭');
INSERT INTO menu_items (menu_id, recipe_id, name, image, unit, price, created_at)
SELECT @menu_m12, id, name, image, unit, price, '2025-11-29 09:00:00'
FROM recipes WHERE name IN ('扬州炒饭','鱼香肉丝','西红柿鸡蛋汤','米饭');

-- 常用ID
SELECT id INTO @u_zhangsan FROM users WHERE login_name='zhangsan';
SELECT id INTO @u_lisi     FROM users WHERE login_name='lisi';
SELECT id INTO @u_wangwu   FROM users WHERE login_name='wangwu';

SELECT mi.id, mi.price INTO @mi_gbjd, @p_gbjd
FROM menu_items mi WHERE mi.menu_id=@menu_333 AND mi.name='宫保鸡丁' LIMIT 1;
SELECT mi.id, mi.price INTO @mi_yzcf, @p_yzcf
FROM menu_items mi WHERE mi.menu_id=@menu_333 AND mi.name='扬州炒饭' LIMIT 1;
SELECT mi.id, mi.price INTO @mi_rice, @p_rice
FROM menu_items mi WHERE mi.menu_id=@menu_333 AND mi.name='米饭' LIMIT 1;
SELECT mi.id, mi.price INTO @mi_jtf, @p_jtf
FROM menu_items mi WHERE mi.menu_id=@menu_333 AND mi.name='鸡腿饭' LIMIT 1;
SELECT mi.id, mi.price INTO @mi_qcdf, @p_qcdf
FROM menu_items mi WHERE mi.menu_id=@menu_333 AND mi.name='青菜豆腐汤' LIMIT 1;


-- 1. 找几个员工和菜单菜品
SELECT id INTO @u_zhangsan FROM users WHERE login_name='zhangsan';
SELECT id INTO @u_lisi     FROM users WHERE login_name='lisi';

-- 用今天的菜单（如果你的菜单名叫 111 且日期是今天）
SELECT id INTO @menu_today FROM menus WHERE date = CURDATE() ORDER BY id LIMIT 1;

-- 取其中几道菜
SELECT id, price INTO @mi_gbjd, @p_gbjd
FROM menu_items WHERE menu_id=@menu_today AND name='宫保鸡丁' LIMIT 1;
SELECT id, price INTO @mi_yzcf, @p_yzcf
FROM menu_items WHERE menu_id=@menu_today AND name='扬州炒饭' LIMIT 1;
SELECT id, price INTO @mi_rice, @p_rice
FROM menu_items WHERE menu_id=@menu_today AND name='米饭' LIMIT 1;

-- 2. 插入两张订单（张三、李四）
SET @d := CURDATE();

-- 张三：宫保鸡丁 + 2 两米饭
INSERT INTO orders (user_id, user_name, phone, order_time, meal_date, total_price, created_at)
VALUES (@u_zhangsan, '张三', '13800000004', NOW(), @d,
        @p_gbjd*1 + @p_rice*2, NOW());
SET @o_zhang := LAST_INSERT_ID();

INSERT INTO order_items (order_id, menu_item_id, name, unit, quantity, price, subtotal) VALUES
                                                                                            (@o_zhang, @mi_gbjd, '宫保鸡丁', '份', 1, @p_gbjd, @p_gbjd),
                                                                                            (@o_zhang, @mi_rice, '米饭',     '两', 2, @p_rice, @p_rice*2);

-- 李四：扬州炒饭
INSERT INTO orders (user_id, user_name, phone, order_time, meal_date, total_price, created_at)
VALUES (@u_lisi, '李四', '13800000005', NOW(), @d,
        @p_yzcf*1, NOW());
SET @o_lisi := LAST_INSERT_ID();

INSERT INTO order_items (order_id, menu_item_id, name, unit, quantity, price, subtotal) VALUES
    (@o_lisi, @mi_yzcf, '扬州炒饭', '份', 1, @p_yzcf, @p_yzcf);


DELETE FROM blanket_orders WHERE date = @d;

DELETE FROM blanket_orders WHERE date = @d;


INSERT INTO blanket_orders (date, item_name, unit, total_quantity, price, subtotal)
SELECT
    @d                         AS date,
    mi.name                    AS item_name,
    mi.unit                    AS unit,
    SUM(oi.quantity)           AS total_quantity,
    mi.price                   AS price,
    SUM(oi.quantity) * mi.price AS subtotal
FROM order_items oi
    JOIN menu_items mi ON oi.menu_item_id = mi.id
    JOIN orders o      ON oi.order_id = o.id
WHERE o.meal_date = @d
GROUP BY mi.name, mi.unit, mi.price;

USE catering_system;

-- 添加工位信息字段
ALTER TABLE orders
    ADD COLUMN work_location VARCHAR(100) DEFAULT NULL
    COMMENT '工位信息（创建订单时保存）'
AFTER phone;