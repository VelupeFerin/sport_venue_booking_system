# 系统配置API优化说明

## 优化目标

将原来分散的系统配置API统一为一个API，实现以下目标：
1. 只用一个API读取全部系统配置
2. 所有用户都可以读取系统配置
3. 只有管理员身份的用户可以编辑系统配置
4. 清理多余的接口
5. 统一接口命名

## 修改内容

### 1. 后端修改

#### 新增文件
- `backend/src/main/java/com/sport_venue_booking_system/controller/SystemConfigController.java`
  - 提供统一的系统配置API
  - `GET /api/config` - 获取所有系统配置（公开API）
  - `PUT /api/config` - 更新系统配置（仅管理员）

#### 修改文件
- `backend/src/main/java/com/sport_venue_booking_system/controller/AdminController.java`
  - 移除重复的系统配置API（`/api/admin/config`）
  - 移除SystemConfigService依赖

- `backend/src/main/java/com/sport_venue_booking_system/controller/UserController.java`
  - 移除重复的business-hours API（`/api/user/business-hours`）
  - 移除SystemConfigService依赖

- `backend/src/main/java/com/sport_venue_booking_system/config/SecurityConfig.java`
  - 更新权限配置，将`/api/user/business-hours`改为`/api/config`

### 2. 前端修改

#### 修改文件
- `frontend/src/api/user.js`
  - 移除`getBusinessHours()`方法
  - 更新`publicApi.getSystemConfig()`调用新的`/api/config`接口
  - 更新导出方法

- `frontend/src/api/admin.js`
  - 更新`getConfig()`和`updateConfig()`调用新的`/api/config`接口
  - 统一API基础URL为`http://localhost:8080/api`

- `frontend/src/views/HomeView.vue`
  - 更新API调用从`publicApi.getBusinessHours()`改为`publicApi.getSystemConfig()`

- `frontend/src/views/booking/BookingView.vue`
  - 更新API调用从`publicApi.getBusinessHours()`改为`publicApi.getSystemConfig()`

- `frontend/src/views/admin/SessionTemplateView.vue`
  - 更新API调用从`getConfig()`改为`publicApi.getSystemConfig()`
  - 移除admin.js中的getConfig导入

- `frontend/src/views/orders/OrdersView.vue`
  - 更新API调用从`getBusinessHours()`改为`publicApi.getSystemConfig()`

## API接口说明

### 新的统一接口

#### 获取系统配置
```
GET /api/config
```
- **权限**：公开API，所有用户都可以访问
- **返回**：所有系统配置的键值对
- **示例返回**：
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    "venue_name": "体育场馆预订系统",
    "max_order_sessions": "3",
    "cancel_time_limit": "4",
    "business_hours": "09:00-21:00"
  }
}
```

#### 更新系统配置
```
PUT /api/config
```
- **权限**：仅管理员可访问（需要ADMIN角色）
- **请求体**：系统配置的键值对
- **示例请求**：
```json
{
  "venue_name": "新场馆名称",
  "max_order_sessions": "5",
  "cancel_time_limit": "6",
  "business_hours": "08:00-22:00"
}
```

### 已移除的接口

1. `GET /api/admin/config` - 管理员获取配置接口
2. `PUT /api/admin/config` - 管理员更新配置接口
3. `GET /api/user/business-hours` - 用户获取营业时间接口

## 权限控制

- **读取配置**：所有用户都可以访问`GET /api/config`
- **编辑配置**：只有具有ADMIN角色的用户才能访问`PUT /api/config`
- **权限验证**：使用Spring Security的`@PreAuthorize("hasRole('ADMIN')")`注解

## 数据格式

系统配置包含以下字段：
- `venue_name` - 场馆名称
- `max_order_sessions` - 单次订单最大场次数
- `cancel_time_limit` - 退订时间要求（小时）
- `business_hours` - 营业时间（格式：HH:MM-HH:MM）

## 兼容性

- 前端所有使用旧API的地方都已更新
- 保持了原有的数据格式和字段名
- 错误处理和默认值逻辑保持不变

## 测试建议

1. 测试未登录用户能否正常读取系统配置
2. 测试普通用户能否读取但不能编辑系统配置
3. 测试管理员用户能否正常读取和编辑系统配置
4. 测试前端页面是否正常显示场馆名称和其他配置
5. 测试系统配置管理页面是否正常工作 