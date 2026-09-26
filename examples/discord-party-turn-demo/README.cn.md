# Discord Party Turn Demo

这是一个用于验证 `room-engine` 基础多人游戏能力的 Discord Activities 派对/回合制 Demo。
第一阶段不依赖真实 Discord OAuth，使用 HTTP 创建房间、加入玩家，使用 WebSocket 驱动回合制游戏。

## 前置条件

- Java 25+
- Maven 3.9+
- Node.js 20+（验证 TypeScript SDK 时使用）

## 启动

在仓库根目录执行：

```bash
mvn -pl examples/discord-party-turn-demo -am install -DskipTests
mvn -f examples/discord-party-turn-demo/pom.xml spring-boot:run
```

默认端口为 `8081`，可以通过 `PORT` 覆盖：

```bash
PORT=8082 mvn -f examples/discord-party-turn-demo/pom.xml spring-boot:run
```

或使用 Docker Compose：

```bash
docker compose -f examples/discord-party-turn-demo/docker-compose.yml up --build
```

Docker Compose 使用 `PORT` 环境变量覆盖宿主机端口，例如：

```bash
PORT=8082 docker compose -f examples/discord-party-turn-demo/docker-compose.yml up --build
```

停止服务：

```bash
docker compose -f examples/discord-party-turn-demo/docker-compose.yml down
```


## 创建房间、加入和退出玩家

创建一个容量为 4 的房间：

```bash
ROOM_ID=$(curl -fsS -X POST \
  'http://localhost:8081/api/v1/party/rooms?capacity=4' \
  | sed -n 's/.*"roomId":"\([^"]*\)".*/\1/p')
echo "$ROOM_ID"
```

加入两个玩家：

```bash
curl -fsS -X POST "http://localhost:8081/api/v1/party/rooms/${ROOM_ID}/players" \
  -H 'Content-Type: application/json' \
  -d '{"playerId":"p1","displayName":"Alice"}'

curl -fsS -X POST "http://localhost:8081/api/v1/party/rooms/${ROOM_ID}/players" \
  -H 'Content-Type: application/json' \
  -d '{"playerId":"p2","displayName":"Bob"}'
```

查询状态：

```bash
curl -fsS "http://localhost:8081/api/v1/party/rooms/${ROOM_ID}"
```
玩家离开和关闭房间：

```bash
curl -fsS -X DELETE "http://localhost:8081/api/v1/party/rooms/${ROOM_ID}/players/p2"
curl -fsS -X DELETE "http://localhost:8081/api/v1/party/rooms/${ROOM_ID}"
```


## WebSocket 协议

客户端连接时必须在 URL 中提供 `roomId` 和 `playerId`：

```text
ws://localhost:8081/ws/party?roomId={ROOM_ID}&playerId=p1
```

服务端会在连接建立时校验玩家属于房间，并立即发送当前 `game_state`。

客户端命令：

```json
{"type":"ping"}
{"type":"ready"}
{"type":"start_game"}
{"type":"turn_action","action":"roll"}
{"type":"turn_action","action":"pass"}
```

规则：

- 至少 2 名玩家才能开始。
- 首位加入房间的玩家是房主。
- 所有玩家必须 `ready` 后，房主才能开始游戏。
- 游戏固定进行 3 轮，每名玩家每轮执行一次操作。
- `roll` 随机获得 1–6 分，`pass` 获得 0 分。
- 只有 `currentPlayerId` 对应的玩家可以执行回合操作。
- 游戏结束后服务端广播 `winner`，不再接受回合操作。

服务端事件示例：

```json
{"type":"pong","timestamp":"2026-09-23T00:00:00Z"}
```

```json
{"type":"game_state","state":{}}
```

```json
{"type":"error","code":"NOT_YOUR_TURN","message":"it is not your turn"}
```

## TypeScript SDK

SDK 位于 `client-sdk-ts`，提供连接、断线退避重连、心跳、Ready、开始游戏和回合操作：

```bash
cd examples/discord-party-turn-demo/client-sdk-ts
npm install
npm run typecheck
```

基本用法：

```ts
import { PartyGameClient } from "./src/index.js";

const client = new PartyGameClient({
  url: "ws://localhost:8081/ws/party",
  roomId: "room-id",
  playerId: "p1",
});

client.onMessage((message) => console.log(message));
client.onConnectionChange((connected) => console.log("connected", connected));
client.connect();
```

连接后可以调用：

```ts
client.ping();
client.ready();
client.startGame();
client.turnAction("roll");
```

## 验证流程

1. 启动后端并创建房间。
2. 加入 `p1` 和 `p2`。
3. 用两个客户端分别连接同一个房间。
4. 两个客户端发送 `ready`。
5. `p1` 发送 `start_game`。
6. 按 `currentPlayerId` 轮流发送 `turn_action`。
7. 确认所有客户端收到一致的 `game_state`。
8. 尝试由非当前玩家操作，确认收到 `NOT_YOUR_TURN`。
9. 完成 3 轮，确认进入 `FINISHED` 且产生 `winner`。
