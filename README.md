# 运动场馆预订系统

## 项目简介

为个体户经营的单一运动场馆（羽毛球、网球、乒乓球、篮球馆等）提供Web端预订管理解决方案，替代传统电话预订模式，支持用户自主订场、商家智能化管理，提升运营效率。

## 技术栈

### 后端
- Spring Boot 3.2.3
- Spring Security
- Spring Data JPA
- MySQL 8.0
- JWT认证
- Maven

### 前端
- Vue 3
- Element Plus
- Vue Router
- Axios
- Vite

## 功能特性

### 已完成功能
- ✅ 用户注册（用户名、密码、手机号验证）
- ✅ 用户登录（JWT认证）
- ✅ 前端表单验证
- ✅ 响应式设计
- ✅ 橙色主题（#FF6633）

### 待开发功能
- 🔄 场地管理
- 🔄 场次预订
- 🔄 订单管理
- 🔄 商家管理功能
- 🔄 系统配置

## 快速开始

### 环境要求
- Node.js >= 16.0.0
- Java 17+
- MySQL 8.0+

### 数据库配置
1. 创建数据库：
```sql
CREATE DATABASE sport_venue_booking_system_database;
```

2. 执行SQL脚本：
```sql
-- 执行 backend/src/main/resources/db/schema.sql
```

### 启动项目

#### 方式一：使用启动脚本（推荐）
```bash
# Windows
start.bat

# Linux/Mac
./start.sh
```

#### 方式二：手动启动

1. 启动后端服务：
```bash
cd backend
mvn spring-boot:run
```

2. 启动前端服务：
```bash
cd frontend
npm install
npm run dev
```

### 访问地址
- 前端：http://localhost:5173
- 后端：http://localhost:8080

## 用户角色

### 普通用户
- 注册账号
- 登录系统
- 查看个人信息

### 商家(管理员)
- 所有普通用户功能
- 订单核验
- 系统配置管理
- 场次模板配置

## API接口

### 认证接口
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录

## 开发说明

### 项目结构
```
├── backend/                 # 后端项目
│   ├── src/main/java/
│   │   └── com/sport_venue_booking_system/
│   │       ├── controller/  # 控制器
│   │       ├── service/     # 业务逻辑
│   │       ├── repository/  # 数据访问
│   │       ├── entity/      # 实体类
│   │       ├── dto/         # 数据传输对象
│   │       ├── config/      # 配置类
│   │       └── utils/       # 工具类
│   └── src/main/resources/
│       └── db/              # 数据库脚本
├── frontend/                # 前端项目
│   ├── src/
│   │   ├── views/           # 页面组件
│   │   ├── components/      # 通用组件
│   │   ├── api/             # API接口
│   │   ├── router/          # 路由配置
│   │   └── assets/          # 静态资源
│   └── package.json
└── README.md
```

### 开发规范
- 使用中文注释
- 遵循RESTful API设计规范
- 前端使用Element Plus组件库
- 统一使用橙色主题色 #FF6633

## 许可证

MIT License

# 项目说明

项目名：基于web的运动场预订系统

这是2025年第2学期的短学期计算机专业课程设计



# 使用说明

### 方法一：从IDE启动



### 向数据库增加示例数据



# 

# 特殊修改

## 前端

Vue有些地方有小问题，在此做出特别说明已经修复方式，以便之后vue更新后取消这些修改

两个按钮垂直排列时，由于element库本身的问题，这两个按钮除了第一个都会出现左边缘12px的空白，因此为第二个按钮添加style="margin-left:0"

el-select组件的问题："在版本 2.5.0之后， el-select 的默认宽度更改为 100%，当使用内联形式时，宽度将显示异常。 为了保持显示正常, 您需要手动配置 el-select 的宽度"（来自element官网）
