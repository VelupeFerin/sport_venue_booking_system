USE
sport_venue_booking_system_database;

-- 用户表
CREATE TABLE IF NOT EXISTS user
(
    id    BIGINT UNSIGNED AUTO_INCREMENT COMMENT '用户ID',
    username  VARCHAR(50)  NOT NULL COMMENT '用户名',
    password   VARCHAR(100) NOT NULL COMMENT 'BCrypt加密密码',
    phone      VARCHAR(20)  NOT NULL COMMENT '联系电话',
    is_admin   BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否为商家(管理员)',
    PRIMARY KEY (id),
    UNIQUE KEY idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户信息表';

-- 场次模板表
CREATE TABLE IF NOT EXISTS session_template
(
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    court_name    VARCHAR(10)   NOT NULL COMMENT '场地名称',
    start_time  TIME           NOT NULL COMMENT '开始时间(HH:00:00，结束时间为1小时后)',
    price       DECIMAL(8, 2) NOT NULL COMMENT '场次价格',
    is_active   BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否开放预订',
    note        TEXT COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uniq_court_start_time (court_name, start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='场次模板配置表';

-- 场次表
CREATE TABLE IF NOT EXISTS session
(
    id   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '场次ID',
    court_name     VARCHAR(10)   NOT NULL COMMENT '场地名称',
    start_time   DATETIME          NOT NULL COMMENT '开始时间(HH:00:00，结束时间为1小时后)',
    price        DECIMAL(8, 2) NOT NULL COMMENT '场次价格',
    is_active   BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否开放预订',
    is_booked    BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已预订，默认否',
    note        TEXT COMMENT '场次备注',
    PRIMARY KEY (id),
    KEY idx_court_booked_time (court_name, is_booked, start_time),
    UNIQUE KEY uniq_court_time (court_name, start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='场次信息表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order`
(
    id    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    user_id     BIGINT UNSIGNED    NOT NULL COMMENT '用户ID',
    total_price DECIMAL(10, 2) NOT NULL COMMENT '总价',
    create_time DATETIME       NOT NULL COMMENT '下单时间',
    verify_time DATETIME DEFAULT NULL COMMENT '核验时间',
    status      ENUM('pending','completed','cancelled') NOT NULL DEFAULT 'pending' COMMENT '订单状态',
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user (id),
    KEY           idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单信息表';

-- 订单场次快照表
CREATE TABLE IF NOT EXISTS order_session
(
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    order_id     BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    court_name     VARCHAR(10)   NOT NULL COMMENT '场地名称',
    start_time   DATETIME          NOT NULL COMMENT '开始时间(结束时间为1小时后)',
    price        DECIMAL(8, 2) NOT NULL COMMENT '场次价格',
    PRIMARY KEY (id),
    FOREIGN KEY (order_id) REFERENCES `order`(id),
    KEY            idx_order_id (order_id,start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单场次快照表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS system_config
(
    config_key   VARCHAR(50)  NOT NULL COMMENT '配置键',
    config_value VARCHAR(100) NOT NULL COMMENT '配置值(程序需按需转换类型)',
    description  VARCHAR(200) NOT NULL COMMENT '描述',
    PRIMARY KEY (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置表';