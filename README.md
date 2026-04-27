# LinkPulse 短链接管理与分析平台

## 1. 项目简介

LinkPulse 是一个基于 Spring Boot 的短链接管理与访问分析平台，支持长链接生成短链接、短链跳转、后台管理、访问统计、过期与禁用控制等功能。

系统针对短链跳转这一高频读场景，引入 Redis 缓存短链映射，降低数据库查询压力；通过 RocketMQ 将访问统计异步化，避免统计写入阻塞跳转主链路；同时使用 Sentinel 对短链跳转和后台创建接口进行 QPS 限流，提高系统在高并发访问场景下的稳定性。

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
- Maven
- Git / GitHub

---

## 3. 核心功能

### 短链管理

- 创建短链接
- 短链接跳转
- 分页查询短链列表
- 按短码和原始链接模糊筛选
- 禁用短链接
- 设置短链接过期时间

### 后台鉴权

- 管理员登录
- JWT Token 签发
- 后台接口鉴权
- 公开跳转接口和受保护管理接口分离

### 缓存优化

- Redis 缓存短码与原始链接映射
- 跳转时优先查 Redis
- Redis miss 时回源 MySQL 并回填缓存
- 禁用短链时主动删除 Redis 缓存，避免缓存脏数据

### 异步统计

- 短链访问事件发送到 RocketMQ
- 消费者异步更新 PV、最近访问时间和访问日志
- 降低跳转主链路的同步写库压力

### 访问分析

- 访问日志记录
- 单条短链统计详情
- 最近访问记录查询
- 热门短链 TopN 排行
- 最近 7 天 PV 趋势统计

### 高并发保护

- Sentinel 对短链跳转接口进行 QPS 限流
- Sentinel 对后台创建短链接口进行 QPS 限流
- 超过阈值时返回 429 Too Many Requests

---

## 4. 系统架构

```text
用户访问短链
     |
     v
RedirectController
     |
     v
Redis 查询短链映射
     |
     |-- hit  -> 直接返回原始链接
     |
     |-- miss -> 查询 MySQL -> 回填 Redis
     |
     v
发送访问事件到 RocketMQ
     |
     v
异步消费者更新 PV / lastAccessTime / access_log