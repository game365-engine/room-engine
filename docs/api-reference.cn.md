# API Reference

## 依赖方式

业务 Spring Boot 项目推荐只依赖 starter：

```xml
<dependency>
    <groupId>com.roomengine</groupId>
    <artifactId>room-engine-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

starter 会带入 core、API 和 WebSocket transport，并自动注册基础 Bean。

## RoomService

`RoomService` 位于 `com.roomengine.core.service`，提供：

```text
Room createRoom(int capacity)
Room joinRoom(String roomId, String playerId, String displayName)
Room leaveRoom(String roomId, String playerId)
void closeRoom(String roomId)
Optional<Room> findRoom(String roomId)
void addListener(RoomEventListener listener)
```

默认使用 `InMemoryPersistenceAdapter`。房间关闭或应用重启后，内存状态会丢失。

## 通用 REST API

基础验证 demo 的默认地址为 `http://localhost:8080`。

### 创建房间

```http
POST /api/v1/rooms?capacity=8
```

成功响应中的 `data` 是房间对象：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "room-id",
    "capacity": 8,
    "status": "CREATED",
    "players": {}
  }
}
```

### 加入房间

```http
POST /api/v1/rooms/{roomId}/players
Content-Type: application/json

{"playerId":"p1","displayName":"Alice"}
```

### 查询房间

```http
GET /api/v1/rooms/{roomId}
```

### 玩家离开

```http
DELETE /api/v1/rooms/{roomId}/players/{playerId}
```

### 关闭房间

```http
DELETE /api/v1/rooms/{roomId}
```

## 响应和错误

成功响应格式：

```json
{"code":200,"message":"success","data":{}}
```

基础错误使用 `ErrorCode`：

| code | 含义 |
|---:|---|
| 1000 | 参数无效 |
| 1001 | 房间不存在 |
| 1002 | 房间已满 |
| 1003 | 玩家已在房间中 |
| 1004 | 房间不接受玩家 |
| 1005 | 玩家不存在 |

错误响应示例：

```json
{"code":1001,"message":"room not found","data":null}
```

## 通用 WebSocket

### 端点

```text
ws://localhost:8080/ws/rooms
```

可以通过 `roomId` 查询参数进行房间分组：

```text
ws://localhost:8080/ws/rooms?roomId=room-id
```

### Ping/Pong

客户端：

```json
{"type":"ping"}
```

服务端：

```json
{"type":"pong","timestamp":"2026-09-25T00:00:00Z"}
```

### 普通消息

发送任意 JSON 对象后，服务端会广播如下 envelope：

```json
{
  "type": "message",
  "from": "session-id",
  "payload": {"type":"custom","value":42}
}
```

带有相同 `roomId` 的连接只会收到同房间消息。未提供 `roomId` 的连接使用全局连接组。

默认 `ConnectionAuthenticator` 放行连接，业务应用必须在生产环境替换它。

## 业务 WebSocket 扩展

业务项目可以像 Party demo 一样注册自己的路径：

```text
ws://localhost:8081/ws/party?roomId={ROOM_ID}&playerId=p1
```

业务 Handler 负责：

- 解析业务命令；
- 校验玩家和游戏状态；
- 调用自己的游戏服务；
- 使用 `MessageCodec` 编解码；
- 调用 transport 提供的连接/广播能力。

## 扩展接口

| 接口 | 用途 |
|---|---|
| `PersistenceAdapter` | 替换房间内存存储 |
| `ConnectionAuthenticator` | WebSocket 身份认证 |
| `MessageCodec` | 自定义消息编解码 |
| `ValidationRule<T>` | 服务端权威业务校验 |
| `ReconnectPolicy` | 断线重连策略 |
