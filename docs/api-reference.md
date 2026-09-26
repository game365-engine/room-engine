# API Reference

## Dependency

Business Spring Boot projects should normally depend only on the starter:

```xml
<dependency>
    <groupId>com.roomengine</groupId>
    <artifactId>room-engine-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

The starter brings in core, API, and WebSocket transport and registers the base beans automatically.

## RoomService

`RoomService` is in `com.roomengine.core.service`:

```text
Room createRoom(int capacity)
Room joinRoom(String roomId, String playerId, String displayName)
Room leaveRoom(String roomId, String playerId)
void closeRoom(String roomId)
Optional<Room> findRoom(String roomId)
void addListener(RoomEventListener listener)
```

The default implementation uses `InMemoryPersistenceAdapter`. Room state is lost when a room is closed or the application restarts.

## Generic REST API

The basic validation demo listens at `http://localhost:8080` by default.

### Create a room

```http
POST /api/v1/rooms?capacity=8
```

Example response:

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

### Join a room

```http
POST /api/v1/rooms/{roomId}/players
Content-Type: application/json

{"playerId":"p1","displayName":"Alice"}
```

### Get a room

```http
GET /api/v1/rooms/{roomId}
```

### Leave a room

```http
DELETE /api/v1/rooms/{roomId}/players/{playerId}
```

### Close a room

```http
DELETE /api/v1/rooms/{roomId}
```

## Responses and Errors

Successful response:

```json
{"code":200,"message":"success","data":{}}
```

Base errors from `ErrorCode`:

| Code | Meaning |
|---:|---|
| 1000 | Invalid argument |
| 1001 | Room not found |
| 1002 | Room is full |
| 1003 | Player is already in the room |
| 1004 | Room is not accepting players |
| 1005 | Player not found |

Example error:

```json
{"code":1001,"message":"room not found","data":null}
```

## Generic WebSocket

### Endpoint

```text
ws://localhost:8080/ws/rooms
```

Group connections by room with the `roomId` query parameter:

```text
ws://localhost:8080/ws/rooms?roomId=room-id
```

### Ping/Pong

Client:

```json
{"type":"ping"}
```

Server:

```json
{"type":"pong","timestamp":"2026-09-25T00:00:00Z"}
```

### Ordinary messages

Any other JSON object is broadcast in this envelope:

```json
{
  "type": "message",
  "from": "session-id",
  "payload": {"type":"custom","value":42}
}
```

Connections with the same `roomId` receive room messages. Connections without a `roomId` use the global group.

The default `ConnectionAuthenticator` allows all connections. Replace it in production.

## Business WebSocket Extension

A business application can register its own path, as shown by the Party demo:

```text
ws://localhost:8081/ws/party?roomId={ROOM_ID}&playerId=p1
```

The business handler is responsible for parsing commands, validating game state, calling its game service, encoding messages, and broadcasting business state.

## Extension Interfaces

| Interface | Purpose |
|---|---|
| `PersistenceAdapter` | Replace in-memory room storage |
| `ConnectionAuthenticator` | Authenticate WebSocket connections |
| `MessageCodec` | Customize message encoding |
| `ValidationRule<T>` | Authoritative server-side validation |
| `ReconnectPolicy` | Reconnect strategy |
