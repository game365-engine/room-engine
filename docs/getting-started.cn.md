# Getting Started

本指南面向从 itch.io 下载 `room-engine` 后第一次运行项目的用户。

## 1. 环境要求

- Java 25+
- Maven 3.9+
- Docker Desktop 或支持 `docker compose` 的 Docker 环境（可选）
- Node.js 20+（只有验证 Party demo 的 TypeScript SDK 时需要）

在仓库根目录确认版本：

```bash
java -version
mvn -version
docker compose version
```

## 2. 运行基础验证 demo

基础验证 demo 用于确认 starter、房间 REST API 和通用 WebSocket 都能工作：

```bash
mvn -pl examples/basic-validation-demo -am install -DskipTests
mvn -f examples/basic-validation-demo/pom.xml spring-boot:run
```

服务默认监听 `8080`，也可以覆盖端口：

```bash
PORT=8082 mvn -f examples/basic-validation-demo/pom.xml spring-boot:run
```

更多接口示例见 [基础验证 demo README](../examples/basic-validation-demo/README.cn.md)。

## 3. 使用 Docker

```bash
docker compose -f examples/basic-validation-demo/docker-compose.yml up --build
```

覆盖宿主机端口：

```bash
PORT=8082 docker compose -f examples/basic-validation-demo/docker-compose.yml up --build
```

停止服务：

```bash
docker compose -f examples/basic-validation-demo/docker-compose.yml down
```

## 4. 验证房间 REST API

创建房间并保存房间 ID：

```bash
ROOM_ID=$(curl -fsS -X POST \
  'http://localhost:8080/api/v1/rooms?capacity=4' \
  | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')
echo "$ROOM_ID"
```

加入玩家并查询：

```bash
curl -fsS -X POST "http://localhost:8080/api/v1/rooms/${ROOM_ID}/players" \
  -H 'Content-Type: application/json' \
  -d '{"playerId":"p1","displayName":"Alice"}'

curl -fsS "http://localhost:8080/api/v1/rooms/${ROOM_ID}"
```

## 5. 参考 Party demo 开发

当基础验证通过后，建议以 `examples/discord-party-turn-demo` 作为业务开发参考：

1. 复制 demo 的 Spring Boot 启动和 WebSocket 配置。
2. 保留对 `room-engine-spring-boot-starter` 的依赖。
3. 将 `PartyGameService` 替换为自己的游戏状态机。
4. 将 Party 消息替换为自己的业务协议。
5. 保留 `RoomService` 管理房间和玩家，不在业务层重新实现房间生命周期。

Party demo 的启动方式：

```bash
mvn -pl examples/discord-party-turn-demo -am install -DskipTests
mvn -f examples/discord-party-turn-demo/pom.xml spring-boot:run
```

## 6. 构建和测试

构建全部 Java 模块并运行测试：

```bash
mvn test
```

验证 Party demo 的客户端 SDK：

```bash
cd examples/discord-party-turn-demo/client-sdk-ts
npm install
npm run typecheck
```

## 7. 从验证 demo 走向生产

当前交付包适合本地开发和单实例验证。生产化时需要至少补充：

- WebSocket 身份认证；
- 外部持久化或房间快照；
- 断线重连和会话恢复；
- 业务级消息校验、限流和日志；
- HTTPS/WSS、反向代理和容器部署配置。
