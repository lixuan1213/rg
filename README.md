# 智能充电桩调度计费系统

## 项目简介

基于 Spring Boot 3.5 + React + TypeScript 的电动汽车充电站管理系统，支持充电请求提交、调度排队（时间优先/优先级优先）、充电控制、分时计费（峰/平/谷）、充电桩管理和账单查询。

## 技术栈

| 层级   | 技术                                   |
| ------ | -------------------------------------- |
| 后端   | Java 24, Spring Boot 3.5, JPA, MySQL  |
| 前端   | React 19, TypeScript, Vite, Ant Design |
| 数据库 | MySQL 8.0+                             |

## 项目结构

```
代码实现/
├── rg-main/          # 后端 Spring Boot 项目
│   ├── src/main/java/com/bupt/charging/
│   │   ├── controller/    # REST 接口
│   │   ├── service/       # 业务逻辑
│   │   ├── repository/    # 数据访问
│   │   ├── entity/        # 实体类
│   │   ├── dto/           # 请求/响应 DTO
│   │   ├── enums/         # 枚举定义
│   │   └── config/        # 配置类（CORS、数据初始化）
│   ├── src/main/resources/
│   │   └── application.yml    # 应用配置
│   └── pom.xml
├── frontend/         # 前端 React 项目
│   └── src/
│       ├── api/      # API 请求封装
│       ├── components/   # 通用组件（AppLayout 导航布局）
│       └── pages/    # 页面
│           ├── Dashboard.tsx              # 系统总览
│           ├── ChargingOperationPage.tsx   # 充电操作（请求+控制合一）
│           ├── PileManagementPage.tsx      # 充电桩管理
│           ├── BillQueryPage.tsx           # 账单查询
│           └── AccountManagementPage.tsx   # 账户管理
└── README.md
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

数据库连接配置在 `rg-main/src/main/resources/application.yml` 中（用户名 `root`，密码请根据实际情况修改）。

### 2. 启动后端

```bash
cd 代码实现/rg-main
mvn spring-boot:run
```

后端运行在 `http://localhost:1985`。首次启动会自动初始化：

- 2 个快充桩（F1、F2，功率 60kW）
- 3 个慢充桩（S1、S2、S3，功率 7kW）
- 分时计费规则（高峰 1.8 元/kWh / 平段 1.2 元/kWh / 低谷 0.6 元/kWh）
- 默认调度策略（时间优先）

### 3. 启动前端

```bash
cd 代码实现/frontend
npm install     # 首次运行需安装依赖
npm run dev
```

前端运行在 `http://localhost:3000`，API 请求自动代理到后端。

## 使用流程

1. **创建账户** — 进入「账户管理」，填写车辆 ID、用户名、电池容量、密码，点击"创建并注册"。成功后自动跳转充电操作页
2. **提交充电请求** — 在「充电操作」页输入车辆 ID、请求电量和充电模式，提交后自动显示车辆状态
3. **开始 / 结束充电** — 系统自动分配充电桩后，同一页面直接点击"开始充电"；充电完毕后点击"结束充电"，系统自动生成账单
4. **查看总览** — 在「系统总览」查看所有充电桩状态，点击桩可展开查看该桩的排队车辆及顺序
5. **查询账单** — 进入「账单查询」，输入车辆 ID 即可查看当天所有账单
6. **管理充电桩** — 进入「充电桩管理」，可开关机、启停桩、上报/恢复故障、设置分时计费规则和调度策略

### 充电流程

```
创建账户 ─→ [自动跳转] ─→ 提交充电请求 ─→ [系统自动调度分配桩]
                                              │
                                    ┌─────────┴──────────┐
                                    ↓ 有空桩              ↓ 无空桩
                                 QUEUED(已排队)        WAITING(等候区)
                                    │                     │
                              开始充电 ←──────────────────┘ 桩空闲自动分配
                                    │
                               CHARGING(充电中)
                                    │
                              结束充电 → 生成账单

## 前端页面功能

| 页面 | 功能 |
|------|------|
| **系统总览** | 充电桩状态表格（展开查看每桩排队车辆及顺序）、排队详情（分配桩/前方车辆/预计等待）、等候区查询、统计卡片，10 秒自动刷新 |
| **充电操作** | 提交充电请求 → 自动查询状态 → 一键开始/结束充电，分配桩自动填入，状态不符时按钮自动禁用 |
| **充电桩管理** | 开关机、启停、故障上报/恢复、分时计费规则设置、调度策略切换 |
| **账单查询** | 输入 carId 查询当天账单，表格直接展示全部字段，无需选日期和点详情 |
| **账户管理** | 创建+注册合二为一，成功后自动跳转充电操作 |

## API 接口

### 充电请求

| 方法 | 路径                            | 说明             |
| ---- | ------------------------------- | ---------------- |
| POST | `/api/charging/request`         | 提交充电请求     |
| PUT  | `/api/charging/amount`          | 修改请求电量     |
| PUT  | `/api/charging/mode`            | 修改充电模式     |
| GET  | `/api/charging/car-state/{carId}` | 查询车辆状态   |
| POST | `/api/charging/start`           | 开始充电         |
| GET  | `/api/charging/state/{carId}`   | 查询充电状态     |
| POST | `/api/charging/end`             | 结束充电         |
| GET  | `/api/charging/bill`            | 查询账单列表     |
| GET  | `/api/charging/bill/{billId}`   | 查询账单详情     |
| GET  | `/api/charging/queue`           | 查询排队状态     |

### 充电桩管理

| 方法 | 路径                            | 说明             |
| ---- | ------------------------------- | ---------------- |
| POST | `/api/pile/{pileId}/power-on`   | 开机             |
| POST | `/api/pile/{pileId}/power-off`  | 关机             |
| POST | `/api/pile/{pileId}/start`      | 启动充电桩       |
| PUT  | `/api/pile/parameters`          | 设置计费参数     |
| GET  | `/api/pile/{pileId}/state`      | 查询单个桩状态   |
| GET  | `/api/pile/state/all`           | 查询所有桩状态   |
| POST | `/api/pile/{pileId}/fault`      | 上报故障         |
| POST | `/api/pile/{pileId}/recover`    | 恢复故障         |

### 账户管理

| 方法 | 路径                       | 说明         |
| ---- | -------------------------- | ------------ |
| POST | `/api/account/create`      | 创建账户     |
| POST | `/api/account/set-pwd`     | 设置密码注册 |
