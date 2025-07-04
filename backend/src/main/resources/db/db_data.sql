USE sport_venue_booking_system_database;

-- 插入初始数据

-- 初始化系统配置
INSERT INTO system_config (config_key, config_value, description)
VALUES ('venue_name', '体育场馆预订系统', '场馆名称'),
       ('max_order_sessions', '3', '单次订单最大场次数'),
       ('cancel_time_limit', '4', '退订时间要求（小时）'),
       ('business_hours', '09:00-21:00', '营业时间');

-- 插入管理员账号，密码为123456，使用BCrypt加密
INSERT INTO user (username, password, phone, is_admin)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '13800138000', TRUE);

-- 插入测试用户数据
INSERT INTO user (username, password, phone, is_admin) VALUES
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '13800138001', FALSE),
('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '13800138002', FALSE);

-- 插入场次模板数据
INSERT INTO session_template (court_name, start_time, price, is_active, note) VALUES
('1号场', '09:00:00', 50.00, TRUE, NULL),
('2号场', '09:00:00', 50.00, TRUE, NULL),
('3号场', '09:00:00', 50.00, TRUE, NULL),
('3号场', '12:00:00', 80.00, FALSE, '维护中'),
('4号场', '09:00:00', 50.00, TRUE, NULL),
('5号场', '09:00:00', 50.00, TRUE, NULL),
('6号场', '20:00:00', 80.00, TRUE, NULL);

-- 插入测试订单数据
INSERT INTO `order` (user_id, total_price, create_time, status) VALUES
(2, 150.00, '2024-01-15 10:30:00', 'pending'),
(3, 120.00, '2024-01-15 11:00:00', 'completed'),
(4, 200.00, '2024-01-15 14:20:00', 'cancelled');

-- 插入订单场次快照数据
INSERT INTO order_session (order_id, court_name, start_time, price) VALUES
(1, '1号场', '2024-01-16 09:00:00', 50.00),
(1, '1号场', '2024-01-16 10:00:00', 50.00),
(1, '1号场', '2024-01-16 11:00:00', 50.00),
(2, '2号场', '2024-01-16 09:00:00', 50.00),
(2, '2号场', '2024-01-16 10:00:00', 50.00),
(3, '1号场', '2024-01-17 09:00:00', 50.00),
(3, '1号场', '2024-01-17 10:00:00', 50.00),
(3, '2号场', '2024-01-17 09:00:00', 50.00),
(3, '2号场', '2024-01-17 10:00:00', 50.00);





