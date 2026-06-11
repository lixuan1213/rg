

## 公开接口 

无需登录即可访问。

| 方法 | 路径                        | 功能说明 |
|------|---------------------------|----------|
| POST | `/api/account/create`     | 用户注册（创建车辆账号） |
| POST | `/api/account/set-pwd`    | 设置密码，完成注册 |
| POST | `/api/account/login`      | 用户登录（`userName` + `password`） |
| POST | `/api/account/adminlogin` | 管理员登录（凭证来自 `application.yml` 配置） |

---

## 用户端接口 

已登录用户专用；请求中的 `carId` 须为**当前登录用户本人**，不得查看或操作他人数据。

| 方法 | 路径 | 功能说明 | 数据隔离 |
|------|------|----------|----------|
| POST | `/api/charging/request` | 提交充电申请 | 仅本人申请 |
| PUT | `/api/charging/amount` | 修改申请电量 | 仅本人修改 |
| PUT | `/api/charging/mode` | 修改充电模式（快充/慢充） | 仅本人修改 |
| GET | `/api/charging/car-state?carId=` | 查询排队/调度状态 | 仅查本人 |
| POST | `/api/charging/start` | 插入充电头，开始充电 | 仅本人操作 |
| GET | `/api/charging/state?carId=` | 查询充电进度 | 仅查本人 |
| POST | `/api/charging/end` | 拔掉充电头，结束充电 | 仅本人操作 |
| GET | `/api/charging/bill?carId=&date=` | 按日期查询账单列表 | 仅本人账单 |


**不属于用户端：**

- `GET /api/charging/queue`（旧路径）→ 返回同模式**全部**车辆，含他人信息，应归为**管理员端**。

---

## 管理员端接口 

已登录管理员专用；具备充电桩管理与全局数据查看权限。

| 方法 | 路径 | 功能说明 |
|------|------|----------|
| POST | `/api/pile/{pileId}/power-on` | 充电桩开机 |
| POST | `/api/pile/{pileId}/power-off` | 充电桩关机 |
| POST | `/api/pile/{pileId}/start` | 启动充电桩 |
| PUT | `/api/pile/parameters` | 设置计费规则与调度策略 |
| GET | `/api/pile/{pileId}/state` | 查询单个充电桩状态 |
| GET | `/api/pile/state/all` | 查询所有充电桩状态 |
| POST | `/api/pile/{pileId}/fault` | 上报充电桩故障 |
| POST | `/api/pile/{pileId}/recover` | 故障恢复 |


| 方法 | 路径 | 功能说明 |
|------|------|----------|
| GET | `/api/charging/queue?mode=` | 查询指定模式（FAST/SLOW）下全部排队/充电车辆 |


| 方法 | 路径                         | 功能说明 |
|------|----------------------------|----------|
| GET | `/api/account/getaccounts` | 查看所有注册用户（`carId`、`userName`、`carCapacity`、`registered`，不含密码） |



| 分类 | 数量     | 说明 |
|------|--------|------|
| 公开 | 4      | 注册、设密、用户登录、管理员登录 |
| 用户端 | 8      | 本人充电全流程 + 本人账单 |
| 管理员端 | 10     | 桩管理 8 + 全局队列 1 + 全部用户 1 |
| **合计** | **22** | — |

