# 智能充电桩调度计费系统

## 项目简介

基于 Spring Boot 3.5 + React + TypeScript 的电动汽车充电站管理系统，支持充电请求提交、调度排队（时间优先/优先级优先）、插拔枪充电控制、自动待拔枪提醒、分时计费（峰/平/谷）、充电桩管理和账单查询。

## 技术栈

| 层级   | 技术                                   |
| ------ | -------------------------------------- |
| 后端   | Java 24, Spring Boot 3.5, JPA, MySQL  |
| 前端   | React 19, TypeScript, Vite, Ant Design |
| 数据库 | MySQL 8.0+                             |

## 项目结构

```
rg/
├── src/main/java/com/bupt/charging/
│   ├── controller/    # REST 接口
│   ├── service/       # 业务逻辑
│   ├── repository/    # 数据访问
│   ├── entity/        # 实体类
│   ├── dto/           # 请求/响应 DTO
│   ├── enums/         # 枚举定义
│   └── config/        # 配置类（CORS、数据初始化、充电进度调度）
├── src/main/resources/
│   └── application.yml    # 应用配置
├── frontend/          # 前端 React 项目
│   └── src/
│       ├── api/       # API 请求封装
│       ├── components/    # 通用组件（AppLayout 导航布局）
│       └── pages/     # 页面
│           ├── Dashboard.tsx              # 系统总览
│           ├── ChargingOperationPage.tsx   # 充电操作（请求+控制合一）
│           ├── PileManagementPage.tsx      # 充电桩管理
│           ├── BillQueryPage.tsx           # 账单查询
│           └── AccountManagementPage.tsx   # 账户管理
└── pom.xml
```

## 环境要求

- JDK 24+
- Maven 3.8+
- Node.js 20+
- MySQL 8.0+

## 快速启动

### 1. 准备数据库

安装并启动 MySQL，创建数据库：

```sql
CREATE DATABASE charging_station CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

数据库连接配置在 `src/main/resources/application.yml` 中。

### 2. 启动后端

```bash
cd rg
mvn spring-boot:run
```

后端运行在 `http://localhost:1985`。首次启动会自动初始化：

- 2 个快充桩（F1、F2，功率 60kW）
- 3 个慢充桩（S1、S2、S3，功率 7kW）
- 分时计费规则（高峰 1.8 元/kWh / 平段 1.2 元/kWh / 低谷 0.6 元/kWh）
- 默认调度策略（时间优先）

### 3. 启动前端

```bash
cd rg/frontend
npm install     # 首次运行需安装依赖
npm run dev
```

前端运行在 `http://localhost:3000`，API 请求自动代理到后端。

## 充电状态流转

```
创建账户 → 提交充电请求 → [系统自动调度]
                              │
                    ┌─────────┴──────────┐
                    ↓ 有空桩              ↓ 无空桩
                 QUEUED(已排队)        WAITING(等候区)
                    │                     │
              开始充电 ←──────────────────┘ 桩空闲自动分配
               (插入充电头)
                    │
               CHARGING(充电中)
                    │
                    ↓ 电量达标（自动）
          PENDING_UNPLUG(待拔枪)  ← 账单已生成，等待用户拔枪
                    │
              结束充电(拔枪)
                    │
               COMPLETED(已完成)  → 车位释放，重新调度
```

## 前端页面功能

| 页面 | 功能 |
|------|------|
| **系统总览** | 充电桩状态表（展开查看排队车辆及顺序）、快慢充排队详情、状态查询（排队状态/充电详情）、统计卡片，10 秒自动刷新 |
| **充电操作** | 提交充电请求 → 自动查询状态 → 开始充电/结束充电/拔枪，修改电量/模式，分配桩自动填入，按钮根据状态自动启用/禁用，待拔枪提醒 |
| **充电桩管理** | 开关机、启停、故障上报/恢复、调度策略与分时计费规则设置 |
| **账单查询** | 按车辆 ID 查当天账单、按账单号查详情弹窗 |
| **账户管理** | 创建+注册一步完成，成功后自动跳转充电操作 |

## API 接口

### 充电请求

| 方法 | 路径 | 参数 | 说明 |
|------|------|------|------|
| POST | `/api/charging/request` | body | 提交充电请求 |
| PUT | `/api/charging/amount` | body | 修改请求电量 |
| PUT | `/api/charging/mode` | body | 修改充电模式 |
| GET | `/api/charging/car-state` | query: `carId` | 查询排队状态 |
| POST | `/api/charging/start` | body | 插入充电头，开始充电 |
| GET | `/api/charging/state` | query: `carId` | 查询充电详情（含拔枪提示） |
| POST | `/api/charging/end` | body | 拔掉充电头，结束充电 |
| GET | `/api/charging/bill` | query: `carId`, `date` | 查询账单列表 |
| GET | `/api/charging/bill/{billId}` | path | 查询账单详情 |
| GET | `/api/charging/queue` | query: `mode` | 查询排队车辆 |

### 充电桩管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/pile/{pileId}/power-on` | 开机 |
| POST | `/api/pile/{pileId}/power-off` | 关机 |
| POST | `/api/pile/{pileId}/start` | 启动充电桩 |
| PUT | `/api/pile/parameters` | 设置计费参数与调度策略 |
| GET | `/api/pile/{pileId}/state` | 查询单个桩状态 |
| GET | `/api/pile/state/all` | 查询所有桩状态 |
| POST | `/api/pile/{pileId}/fault` | 上报故障 |
| POST | `/api/pile/{pileId}/recover` | 恢复故障 |

### 账户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/account/create` | 创建账户 |
| POST | `/api/account/set-pwd` | 设置密码注册 |

## 枚举值

### 车辆状态 (CarState)

| 值 | 说明 |
|----|------|
| WAITING | 等候区，等待系统调度 |
| QUEUED | 已分配到桩，排队等待插入充电头 |
| CHARGING | 充电中 |
| PENDING_UNPLUG | 充电完成，等待拔枪（账单已生成） |
| COMPLETED | 已拔枪，请求归档 |
| CANCELLED | 已取消 |

### 充电桩状态 (PileWorkingState)

| 值 | 说明 |
|----|------|
| OFF | 关机 |
| IDLE | 空闲，可接受新充电头插入 |
| CHARGING | 充电中 |
| WAITING_UNPLUG | 等待拔枪，下一位用户暂不可插入 |
| FAULT | 故障 |
