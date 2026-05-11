# LinkPulse 短链接管理与访问分析平台

## 1. 项目简介

LinkPulse 是一个基于 Spring Boot 的短链接管理与访问分析平台，支持长链接生成短链接、短链跳转、后台管理、访问统计、过期与禁用控制等功能。

系统针对短链跳转这一高频读场景，引入 Redis 缓存短链映射，降低数据库查询压力；通过 RocketMQ 将访问统计异步化，避免统计写入阻塞跳转主链路；同时使用 Sentinel 对短链跳转和后台创建接口进行 QPS 限流，提高系统在高并发访问场景下的稳定性。

访问日志属于高写入、高增长数据，因此系统进一步引入 ShardingSphere，对访问日志表进行水平分表，提升访问日志持续增长场景下的数据扩展能力。

---

## 2. 技术栈

- Java 17
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- MySQL
- Redis
- RocketMQ
- Sentinel
- ShardingSphere-JDBC
- Swagger / OpenAPI
- Maven
- Git / GitHub
- Postman

---

## 3. 核心功能

### 3.1 短链管理

- 创建短链接
- 短链接跳转
- 分页查询短链列表
- 按短码和原始链接模糊筛选
- 禁用短链接
- 设置短链接过期时间
- 跳转时校验短链状态和过期时间

### 3.2 后台鉴权

- 管理员登录
- JWT Token 签发
- 后台接口鉴权
- 公开跳转接口和受保护管理接口分离
- 通过 Spring Security 统一管理接口访问权限

### 3.3 缓存优化

- Redis 缓存短码与原始链接映射
- 跳转时优先查询 Redis
- Redis miss 时回源 MySQL 并回填 Redis
- 禁用短链时主动删除 Redis 缓存，避免缓存脏数据

### 3.4 异步统计

- 短链访问事件发送到 RocketMQ
- 消费者异步更新 PV、最近访问时间和访问日志
- 降低跳转主链路的同步写库压力
- 将跳转链路和统计写入链路解耦

### 3.5 访问分析

- 单条短链统计详情
- 统计概览接口
- 最近 7 天 PV 趋势统计
- 访问日志分页查询
- Referer 来源统计
- User-Agent 客户端类型统计
- 热门短链 TopN 排行

统计概览支持查询：

- 总 PV
- 今日 PV
- 昨日 PV
- 近 7 日 PV
- 独立 IP 数
- 最近访问时间

Referer 来源统计支持将直接访问归类为：

```text
direct
```

User-Agent 客户端统计支持将访问客户端归类为：

```text
Browser
curl
Postman
Other
unknown
```

### 3.6 高并发保护

- Sentinel 对短链跳转接口进行 QPS 限流
- Sentinel 对后台创建短链接口进行 QPS 限流
- 超过阈值时返回 `429 Too Many Requests`
- 防止热点短链或恶意请求冲击后端服务

### 3.7 访问日志分表

系统使用 ShardingSphere-JDBC 对访问日志表进行水平分表。

逻辑表：

```text
short_link_access_log
```

真实表：

```text
short_link_access_log_0
short_link_access_log_1
```

分片键：

```text
short_link_id
```

分片规则：

```text
short_link_id % 2
```

路由规则：

```text
偶数 short_link_id -> short_link_access_log_0
奇数 short_link_id -> short_link_access_log_1
```

---

## 4. 系统架构

### 4.1 短链跳转链路

```text
用户访问短链
     |
     v
RedirectController
     |
     v
Sentinel 判断是否限流
     |
     v
Redis 查询短链映射
     |
     |-- hit  -> 直接返回原始链接
     |
     |-- miss -> 查询 MySQL -> 回填 Redis
     |
     v
返回 302 重定向
     |
     v
发送访问事件到 RocketMQ
     |
     v
异步消费者更新 PV / lastAccessTime / access_log
```

### 4.2 后台管理链路

```text
管理员登录
     |
     v
获取 JWT Token
     |
     v
访问 /api/admin/**
     |
     v
JwtAuthenticationFilter 校验 Token
     |
     v
执行短链管理 / 统计查询接口
```

### 4.3 访问日志分表链路

```text
业务代码操作逻辑表 short_link_access_log
     |
     v
ShardingSphere 根据 short_link_id % 2 路由
     |
     |-- 偶数 short_link_id -> short_link_access_log_0
     |
     |-- 奇数 short_link_id -> short_link_access_log_1
```

---

## 5. 数据库表设计

### 5.1 short_link

短链接主表，用于保存短链基础信息。

主要字段：

| 字段 | 说明 |
|---|---|
| id | 主键 |
| short_code | 短码 |
| original_url | 原始链接 |
| status | 状态，1 表示启用，0 表示禁用 |
| pv | 访问量 |
| last_access_time | 最近访问时间 |
| expire_time | 过期时间，NULL 表示不过期 |
| created_at | 创建时间 |
| updated_at | 更新时间 |

### 5.2 short_link_access_log

访问日志逻辑表，用于记录短链访问明细。

主要字段：

| 字段 | 说明 |
|---|---|
| id | 主键 |
| short_link_id | 短链 ID |
| short_code | 短码 |
| original_url | 原始链接 |
| client_ip | 访问 IP |
| user_agent | 客户端 User-Agent |
| referer | 访问来源 |
| access_time | 访问时间 |

实际分表：

```text
short_link_access_log_0
short_link_access_log_1
```

### 5.3 sys_user

后台管理员用户表。

主要字段：

| 字段 | 说明 |
|---|---|
| id | 主键 |
| username | 用户名 |
| password | 加密后的密码 |
| role | 角色 |
| status | 用户状态 |
| created_at | 创建时间 |
| updated_at | 更新时间 |

---

## 6. 核心接口

### 6.1 登录

```http
POST /api/auth/login
```

请求示例：

```json
{
  "username": "admin",
  "password": "Admin@123456"
}
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "jwt-token",
    "username": "admin",
    "role": "ADMIN"
  }
}
```

---

### 6.2 创建短链

```http
POST /api/admin/links
Authorization: Bearer token
```

请求示例：

```json
{
  "originalUrl": "https://www.baidu.com",
  "expireTime": "2026-05-01T23:59:59"
}
```

---

### 6.3 分页查询短链

```http
GET /api/admin/links?pageNum=1&pageSize=10
Authorization: Bearer token
```

---

### 6.4 条件筛选短链

```http
GET /api/admin/links?pageNum=1&pageSize=10&shortCode=773&originalUrl=meituan
Authorization: Bearer token
```

---

### 6.5 禁用短链

```http
PATCH /api/admin/links/{id}/disable
Authorization: Bearer token
```

---

### 6.6 短链跳转

```http
GET /{shortCode}
```

示例：

```http
GET /773ec2
```

正常情况下返回：

```http
302 Found
```

如果短链不存在、已禁用或已过期，返回：

```http
404 Not Found
```

如果触发 Sentinel 限流，返回：

```http
429 Too Many Requests
```

---

## 7. 访问统计接口

### 7.1 单条短链统计详情

```http
GET /api/admin/stats/links/{id}
Authorization: Bearer token
```

---

### 7.2 统计概览

```http
GET /api/admin/stats/links/{id}/overview
Authorization: Bearer token
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "shortCode": "773ec2",
    "originalUrl": "https://www.meituan.com",
    "shortUrl": "http://localhost:8080/773ec2",
    "totalPv": 27,
    "todayPv": 3,
    "yesterdayPv": 0,
    "last7DaysPv": 3,
    "uniqueIpCount": 1,
    "lastAccessTime": "2026-05-06T19:27:14"
  }
}
```

---

### 7.3 最近 7 天 PV 趋势

```http
GET /api/admin/stats/links/{id}/pv-trend?days=7
Authorization: Bearer token
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "date": "2026-04-21",
      "pv": 0
    },
    {
      "date": "2026-04-22",
      "pv": 3
    },
    {
      "date": "2026-04-27",
      "pv": 6
    }
  ]
}
```

---

### 7.4 访问日志分页

```http
GET /api/admin/stats/links/{id}/logs?pageNum=1&pageSize=10
Authorization: Bearer token
```

---

### 7.5 Referer 来源统计

```http
GET /api/admin/stats/links/{id}/referers
Authorization: Bearer token
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "referer": "direct",
      "pv": 22
    },
    {
      "referer": "https://github.com",
      "pv": 1
    }
  ]
}
```

---

### 7.6 User-Agent 客户端统计

```http
GET /api/admin/stats/links/{id}/user-agents
Authorization: Bearer token
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "clientType": "Browser",
      "pv": 14
    },
    {
      "clientType": "curl",
      "pv": 10
    },
    {
      "clientType": "Postman",
      "pv": 1
    }
  ]
}
```

---

### 7.7 热门短链 TopN

```http
GET /api/admin/stats/top-links?limit=5
Authorization: Bearer token
```

---

## 8. 中间件设计说明

### 8.1 Redis 缓存设计

短链跳转属于高频读场景，因此使用 Redis 缓存短码到原始链接的映射。

Key 设计：

```text
short_link:{shortCode}
```

示例：

```text
short_link:773ec2 -> https://www.meituan.com
```

跳转流程：

1. 根据 shortCode 查询 Redis。
2. Redis 命中则直接返回原始链接。
3. Redis 未命中则查询 MySQL。
4. MySQL 查询成功后回填 Redis。
5. 短链禁用时删除对应 Redis key。

---

### 8.2 RocketMQ 异步统计与幂等设计

短链跳转接口是系统核心主链路，如果在跳转时同步更新 PV 和访问日志，会增加接口耗时。

因此系统将访问统计设计为异步链路：

```text
用户访问短链
     |
     v
主链路完成短链校验和 302 跳转
     |
     v
生成 ShortLinkAccessEvent 访问事件
     |
     v
发送到 RocketMQ
     |
     v
消费者异步更新 PV / lastAccessTime / access_log
```

该设计可以降低跳转接口的同步写库压力，使统计逻辑和跳转逻辑解耦。

同时，RocketMQ 在网络抖动、消费者异常重试等场景下可能出现重复投递或重复消费。为了避免同一条访问事件被重复统计，系统在访问事件中引入 `eventId` 作为幂等键。

幂等处理流程：

```text
消费者接收 ShortLinkAccessEvent
     |
     v
读取 eventId
     |
     v
根据 shortLinkId + eventId 查询访问日志是否已存在
     |
     |-- 已存在 -> 说明该消息已处理，直接忽略
     |
     |-- 不存在 -> 更新 PV，并插入访问日志
```

访问日志表新增字段：

```text
event_id
```

并在真实分表中建立唯一索引：

```text
short_link_access_log_0(event_id)
short_link_access_log_1(event_id)
```

通过 `eventId` 幂等校验，可以避免 RocketMQ 重复消费导致 PV 重复累加和访问日志重复插入。

---

### 8.3 Sentinel 限流设计

系统使用 Sentinel 对高频接口进行 QPS 限流。

当前限流资源：

```text
short-link-redirect
admin-create-short-link
```

限流策略：

| 资源 | 接口 | 阈值 |
|---|---|---|
| short-link-redirect | GET /{shortCode} | QPS = 5 |
| admin-create-short-link | POST /api/admin/links | QPS = 2 |

超过阈值时返回：

```http
429 Too Many Requests
```

---

### 8.4 ShardingSphere 分表设计

访问日志数据会随着短链访问持续增长，因此系统使用 ShardingSphere-JDBC 对访问日志表进行水平分表。

逻辑表：

```text
short_link_access_log
```

真实表：

```text
short_link_access_log_0
short_link_access_log_1
```

分片规则：

```text
short_link_id % 2
```

ShardingSphere 接入后，业务代码仍然操作逻辑表 `short_link_access_log`，由 ShardingSphere 自动路由到真实分表。

---

## 9. Swagger / OpenAPI 接口文档

项目已接入 Swagger / OpenAPI，启动后可访问：

```text
http://localhost:8080/swagger-ui/index.html
```

后台接口已配置 Bearer Token 鉴权。

使用方式：

1. 调用登录接口获取 JWT。
2. 在 Swagger 页面点击 `Authorize`。
3. 填入 JWT Token。
4. 在线调试后台管理和访问统计接口。

---

## 10. Postman 接口集合

项目提供 Postman Collection，位于：

```text
docs/postman/LinkPulse.postman_collection.json
```

导入 Postman 后，可以测试：

- 登录鉴权
- 短链创建
- 短链分页查询
- 短链禁用
- 统计详情
- 统计概览
- PV 趋势
- 访问日志分页
- Referer 来源统计
- User-Agent 统计
- 热门短链 TopN

---

## 11. 本地启动说明

### 11.1 启动 MySQL

确保本地 MySQL 已启动，并存在数据库：

```sql
CREATE DATABASE linkpulse;
```

如果需要创建项目专用用户，可以执行：

```sql
CREATE USER IF NOT EXISTS 'linkpulse_user'@'localhost' IDENTIFIED BY 'Linkpulse@123456';
GRANT ALL PRIVILEGES ON linkpulse.* TO 'linkpulse_user'@'localhost';
FLUSH PRIVILEGES;
```

> 注意：公开仓库中不要提交生产环境密码。上述账号密码仅用于本地开发测试。

---

### 11.2 启动 Redis

如果 Redis 运行在 WSL 中：

```bash
sudo service redis-server start
redis-cli ping
```

返回：

```text
PONG
```

---

### 11.3 启动 RocketMQ

```bash
cd ~/rocketmq-all-5.5.0-bin-release

nohup sh bin/mqnamesrv &

nohup sh bin/mqbroker -n 127.0.0.1:9876 --enable-proxy -c conf/linkpulse-broker.conf &
```

创建 Topic 和消费组：

```bash
sh bin/mqadmin updateTopic -n 127.0.0.1:9876 -c DefaultCluster -t link-access-topic

sh bin/mqadmin updateSubGroup -n 127.0.0.1:9876 -c DefaultCluster -g linkpulse-access-event-consumer
```

---

### 11.4 启动 Spring Boot

方式一：IDEA 中运行：

```text
LinkpulseApplication
```

方式二：命令行运行：

```bash
./mvnw spring-boot:run
```

Windows PowerShell：

```powershell
.\mvnw spring-boot:run
```

---

## 12. 测试与验证结果

### 12.1 JWT 鉴权验证

不带 token 访问后台接口：

```http
401 Unauthorized
```

带合法 token 访问后台接口：

```http
200 OK
```

---

### 12.2 Sentinel 限流验证

连续快速请求短链跳转接口：

```powershell
1..20 | ForEach-Object {
    curl.exe -s -o NUL -w "%{http_code}`n" http://localhost:8080/773ec2
}
```

预期返回中会出现：

```text
302
302
302
429
429
```

说明 Sentinel QPS 限流生效。

---

### 12.3 ShardingSphere 分表验证

访问短链后，查询分表数量：

```sql
SELECT 'short_link_access_log' AS table_name, COUNT(*) AS cnt
FROM short_link_access_log

UNION ALL

SELECT 'short_link_access_log_0' AS table_name, COUNT(*) AS cnt
FROM short_link_access_log_0

UNION ALL

SELECT 'short_link_access_log_1' AS table_name, COUNT(*) AS cnt
FROM short_link_access_log_1;
```

示例结果：

```text
short_link_access_log      19
short_link_access_log_0     2
short_link_access_log_1    22
```

说明新的访问日志已经通过 ShardingSphere 写入分表。

---

### 12.4 Swagger 接口验证

Swagger 页面可正常访问：

```text
http://localhost:8080/swagger-ui/index.html
```

并支持 Bearer Token 鉴权后在线调试后台接口。

---
### 12.5 RocketMQ 重复消费幂等验证

为了验证 RocketMQ 异步统计链路的幂等性，系统提供了本地测试接口：

```http
GET /mq-duplicate-test
```

该接口会构造两条完全相同的访问事件消息，并发送到 RocketMQ：

```text
相同 shortLinkId
相同 shortCode
相同 referer
相同 eventId
```

预期结果：

```text
第一条消息正常消费，更新 PV 并插入访问日志；
第二条消息被识别为重复消息，直接忽略，不再重复统计。
```

验证 SQL：

```sql
SELECT event_id, COUNT(*) AS cnt
FROM short_link_access_log_1
WHERE referer = 'mq-duplicate-test'
GROUP BY event_id
ORDER BY MAX(access_time) DESC;
```

验证结果示例：

```text
event_id                              cnt
bd4f3ca6-...                           1
```

该结果说明：同一个 `eventId` 的重复 MQ 消息最终只落库一次，消费者幂等逻辑生效。
> `/mq-test` 和 `/mq-duplicate-test` 仅用于本地开发环境验证 RocketMQ 消息发送与幂等消费逻辑，Controller 已通过 `@Profile("dev")` 限制只在 dev 环境启用，生产环境不暴露该测试接口。
## 13. 轻量压测结果

本项目使用 `curl` 对核心接口进行了轻量压测，主要验证短链跳转性能、Sentinel 限流效果、统计接口响应耗时以及 ShardingSphere 分表写入情况。

压测结果文件保存在：

```text
docs/benchmark
```

当前包含：

```text
docs/benchmark/redirect_latency.csv
docs/benchmark/sentinel_redirect_limit.txt
docs/benchmark/stats_overview_latency.csv
```

---

### 13.1 短链跳转耗时

测试目标：

```http
GET /773ec2
```

测试命令：

```powershell
1..10 | ForEach-Object {
    curl.exe -s -o NUL -w "%{http_code},%{time_total}`n" http://localhost:8080/773ec2
    Start-Sleep -Milliseconds 300
} | Out-File -Encoding utf8 docs\benchmark\redirect_latency.csv
```

测试结果：

```text
302,0.019023
302,0.008972
302,0.009245
302,0.010099
302,0.009318
302,0.008309
302,0.009593
302,0.008752
302,0.009024
302,0.008572
```

结论：

```text
短链跳转接口 10 次请求均返回 302，响应时间主要集中在 8ms–10ms，平均约 10.09ms。
```

说明短链跳转链路在本地测试环境下可以稳定完成 Redis 查询、短链校验、RocketMQ 访问事件发送和 302 重定向。

---

### 13.2 Sentinel 限流验证

测试目标：

```http
GET /773ec2
```

测试命令：

```powershell
1..20 | ForEach-Object {
    curl.exe -s -o NUL -w "%{http_code}`n" http://localhost:8080/773ec2
} | Out-File -Encoding utf8 docs\benchmark\sentinel_redirect_limit.txt
```

测试结果：

```text
302
302
302
302
302
429
429
429
429
429
429
429
429
429
429
429
429
429
429
429
```

结论：

```text
连续快速请求短链跳转接口时，前 5 次返回 302，随后请求返回 429，说明 Sentinel 对短链跳转接口的 QPS 限流规则生效。
```

该结果对应当前 Sentinel 配置：

```text
short-link-redirect QPS = 5
```

当访问频率超过阈值后，系统直接返回：

```http
429 Too Many Requests
```

从而避免热点短链或恶意请求持续冲击后端服务。

---

### 13.3 统计概览接口耗时

测试目标：

```http
GET /api/admin/stats/links/1/overview
Authorization: Bearer token
```

测试命令：

```powershell
1..10 | ForEach-Object {
    curl.exe -s -o NUL -w "%{http_code},%{time_total}`n" `
      -H "Authorization: Bearer $token" `
      http://localhost:8080/api/admin/stats/links/1/overview
    Start-Sleep -Milliseconds 300
} | Out-File -Encoding utf8 docs\benchmark\stats_overview_latency.csv
```

测试结果：

```text
200,0.017222
200,0.013059
200,0.014452
200,0.013775
200,0.013448
200,0.012684
200,0.013113
200,0.012863
200,0.014121
200,0.012794
```

结论：

```text
统计概览接口 10 次请求均返回 200，在 JWT 鉴权和 ShardingSphere 逻辑表查询下，响应时间主要集中在 12ms–17ms，平均约 13.75ms。
```

该接口会返回：

```text
总 PV
今日 PV
昨日 PV
近 7 日 PV
独立 IP 数
最近访问时间
```

说明后台统计接口在经过 JWT 鉴权、数据库聚合查询和 ShardingSphere 分表路由后，仍能保持稳定响应。

---

### 13.4 ShardingSphere 分表验证

访问日志逻辑表：

```text
short_link_access_log
```

真实分表：

```text
short_link_access_log_0
short_link_access_log_1
```

分片规则：

```text
short_link_id % 2
```

验证 SQL：

```sql
SELECT 'short_link_access_log' AS table_name, COUNT(*) AS cnt
FROM short_link_access_log

UNION ALL

SELECT 'short_link_access_log_0' AS table_name, COUNT(*) AS cnt
FROM short_link_access_log_0

UNION ALL

SELECT 'short_link_access_log_1' AS table_name, COUNT(*) AS cnt
FROM short_link_access_log_1;
```

示例验证结果：

```text
short_link_access_log      19
short_link_access_log_0     2
short_link_access_log_1    22
```

说明：

```text
原始 short_link_access_log 表保留历史数据；
新的访问日志已经通过 ShardingSphere 路由写入 short_link_access_log_0 和 short_link_access_log_1 分表；
业务代码仍然操作逻辑表 short_link_access_log，由 ShardingSphere 自动完成真实表路由。
```

---

### 13.5 压测结论

本地轻量压测表明：

- 短链跳转接口在非限流情况下能够稳定返回 `302`。
- Redis 缓存和 RocketMQ 异步统计没有明显阻塞跳转主链路。
- Sentinel 能够在连续高频请求下返回 `429 Too Many Requests`，限流规则生效。
- 统计概览接口在 JWT 鉴权和 ShardingSphere 查询链路下能够稳定返回 `200`。
- 访问日志已经通过 ShardingSphere 写入分表，具备一定的数据扩展能力。

---

## 14. 项目亮点

- 基于 Spring Security + JWT 实现后台接口鉴权。
- 使用 Redis 缓存短链映射，降低高频跳转场景下的数据库查询压力。
- 使用 RocketMQ 将访问统计异步化，降低跳转主链路同步写库压力，并通过 eventId 幂等键避免 MQ 重复消费导致 PV 重复累加和访问日志重复插入。
- 针对 RocketMQ 可能重复投递的问题，在访问事件中引入 eventId，并在消费者侧基于 shortLinkId + eventId 做幂等校验，提升异步统计链路的数据一致性。
- 使用 Sentinel 对短链跳转和后台创建接口进行 QPS 限流。
- 使用 ShardingSphere 对访问日志表进行水平分表，支撑日志数据持续增长。
- 实现多维访问分析能力，包括 PV 趋势、统计概览、访问日志分页、Referer 来源统计、User-Agent 统计和热门短链排行。
- 接入 Swagger / OpenAPI 和 Postman Collection，方便接口调试和项目展示。
- 增加全局异常处理机制，统一拦截业务异常、参数校验异常和系统异常，避免接口返回 Whitelabel Error Page，提升后端服务的规范性。
