# AGENTS.md

## Cursor Cloud specific instructions

### 产品概述

本仓库是 **charging-station**（智能充电桩调度计费系统）——一个 Spring Boot REST API 后端，无前端、无 Docker Compose、无自动化测试。端到端验证通过 HTTP API（curl/Postman）完成。

### 必需服务

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL 8 | 3306 | 数据库 `charging_station`，凭据见 `src/main/resources/application.yml` |
| Spring Boot API | 1985 | 主应用，暴露 `/api/account`、`/api/charging`、`/api/pile` |

### Java 版本

`pom.xml` 要求 **Java 24**。Ubuntu apt 暂无 JDK 24，请通过 SDKMAN 安装 **Java 25**（`25.0.2-open`），可编译 `--release 24`：

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk use java 25.0.2-open
```

### MySQL 启动（无 systemd 环境）

Cloud VM 通常无法使用 `systemctl`。若 MySQL 未运行：

```bash
sudo mysqld_safe --datadir=/var/lib/mysql --pid-file=/var/run/mysqld/mysqld.pid &
```

首次需确保 root 密码与 `application.yml` 一致，并创建数据库：

```bash
sudo mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '<password-from-application.yml>'; CREATE DATABASE IF NOT EXISTS charging_station CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; FLUSH PRIVILEGES;"
```

### 常用命令

标准命令见 `pom.xml` 与 Spring Boot 惯例：

| 操作 | 命令 |
|------|------|
| 构建 | `mvn -DskipTests package` |
| 运行 | `mvn spring-boot:run` |
| 测试 | `mvn test`（仓库内无 `src/test`，会空跑通过） |

应用启动后监听 `http://localhost:1985`。首次启动时 `DataInitializer` 会种子化 2 个快充桩（F1–F2）、3 个慢充桩（S1–S3）、计费规则与调度策略。

### 典型 API 验证流程

```bash
# 创建账户 → 设置密码 → 提交充电请求
curl -X POST http://localhost:1985/api/account/create \
  -H "Content-Type: application/json" \
  -d '{"carId":"CAR001","userName":"测试","carCapacity":60.0}'

curl -X POST http://localhost:1985/api/account/set-pwd \
  -H "Content-Type: application/json" \
  -d '{"carId":"CAR001","password":"test1234"}'

curl -X POST http://localhost:1985/api/charging/request \
  -H "Content-Type: application/json" \
  -d '{"carId":"CAR001","requestAmount":30.0,"requestMode":"FAST"}'
```

充电请求 DTO 字段为 `requestMode`（非 `chargingMode`），取值 `FAST` 或 `SLOW`。

### 注意事项

- 无 ESLint/Checkstyle 等 lint 配置；代码质量检查依赖 Maven 编译。
- 长时间运行应用请使用 tmux 会话：`mvn spring-boot:run`。
- CORS 已对 `/api/**` 开放，可接外部前端客户端。
