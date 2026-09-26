# Basic Validation Demo

这是 `room-engine` 的最小基础能力验证应用，用于确认 Spring Boot starter、房间 REST API 和通用 WebSocket 传输可以正常运行。

它不是具体游戏模板。需要实现游戏规则、业务消息和客户端 SDK 时，请参考同级的 `examples/discord-party-turn-demo`。

## 前置条件

- Java 25+
- Maven 3.9+
- Docker（使用容器启动时）

## 启动

在仓库根目录执行：

```bash
mvn -pl examples/basic-validation-demo -am install -DskipTests
mvn -f examples/basic-validation-demo/pom.xml spring-boot:run
```

默认端口为 `8080`，可以通过 `PORT` 覆盖：

```bash
PORT=8082 mvn -f examples/basic-validation-demo/pom.xml spring-boot:run
```

## Docker Compose

```bash
docker compose -f examples/basic-validation-demo/docker-compose.yml up --build
```

使用 `PORT` 覆盖宿主机端口：

```bash
PORT=8082 docker compose -f examples/basic-validation-demo/docker-compose.yml up --build
```

停止服务：

```bash
docker compose -f examples/basic-validation-demo/docker-compose.yml down
```

## 房间 REST API

创建一个容量为 4 的房间：

```bash
ROOM_ID=$(curl -fsS -X POST \
  'http://localhost:8080/api/v1/rooms?capacity=4' \
  | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')
echo "$ROOM_ID"
```

加入玩家：

```bash
curl -fsS -X POST "http://localhost:8080/api/v1/rooms/${ROOM_ID}/players" \
  -H 'Content-Type: application/json' \
  -d '{"playerId":"p1","displayName":"Alice"}'

curl -fsS -X POST "http://localhost:8080/api/v1/rooms/${ROOM_ID}/players" \
  -H 'Content-Type: application/json' \
  -d '{"playerId":"p2","displayName":"Bob"}'
```

查询房间：

```bash
curl -fsS "http://localhost:8080/api/v1/rooms/${ROOM_ID}"
```

玩家离开和关闭房间：

```bash
curl -fsS -X DELETE "http://localhost:8080/api/v1/rooms/${ROOM_ID}/players/p2"
curl -fsS -X DELETE "http://localhost:8080/api/v1/rooms/${ROOM_ID}"
```

接口返回统一的 `code`、`message` 和 `data` 字段。房间容量、重复玩家、房间不存在等错误由 `room-engine-core` 和 starter 统一处理。

## 通用 WebSocket

基础 transport 注册 `/ws/rooms` 端点。连接示例：

```text
ws://localhost:8080/ws/rooms?roomId={ROOM_ID}
```

发送心跳：

```json
{"type":"ping"}
```

服务端返回：

```json
{"type":"pong","timestamp":"2026-09-25T00:00:00Z"}
```

其他 JSON 消息会被封装为通用消息并广播给连接。按 `roomId` 连接时，只会广播给同一房间的连接；未提供 `roomId` 的连接属于全局连接。

这个端点只验证 transport 能力，不提供玩家身份认证和具体游戏协议。业务项目应在自己的 WebSocket Handler 中实现认证、命令路由和状态广播，具体做法可参考 Party demo。

## 验证流程

1. 启动应用或 Docker Compose。
2. 创建房间并记录返回的房间 ID。
3. 加入两个玩家。
4. 查询房间，确认玩家列表和房间状态。
5. 使用 WebSocket 客户端连接 `/ws/rooms`，发送 `ping`。
6. 打开两个同房间连接，发送普通 JSON 消息，确认只在房间内广播。
7. 删除玩家并关闭房间。
