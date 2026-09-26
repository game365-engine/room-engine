# Basic Validation Demo

This is the smallest runnable validation application for `room-engine`. It verifies the Spring Boot starter, room REST API, and generic WebSocket transport.

It is not a game template. For game rules, business messages, and a client SDK, use the sibling `examples/discord-party-turn-demo` as the reference.

## Requirements

- Java 25+
- Maven 3.9+
- Docker (for container startup)

## Start

From the repository root:

```bash
mvn -pl examples/basic-validation-demo -am install -DskipTests
mvn -f examples/basic-validation-demo/pom.xml spring-boot:run
```

The default port is `8080`:

```bash
PORT=8082 mvn -f examples/basic-validation-demo/pom.xml spring-boot:run
```

## Docker Compose

```bash
docker compose -f examples/basic-validation-demo/docker-compose.yml up --build
```

Override the host port:

```bash
PORT=8082 docker compose -f examples/basic-validation-demo/docker-compose.yml up --build
```

Stop the service:

```bash
docker compose -f examples/basic-validation-demo/docker-compose.yml down
```

## Room REST API

Create a room with capacity 4:

```bash
ROOM_ID=$(curl -fsS -X POST \
  'http://localhost:8080/api/v1/rooms?capacity=4' \
  | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')
echo "$ROOM_ID"
```

Join players:

```bash
curl -fsS -X POST "http://localhost:8080/api/v1/rooms/${ROOM_ID}/players" \
  -H 'Content-Type: application/json' \
  -d '{"playerId":"p1","displayName":"Alice"}'

curl -fsS -X POST "http://localhost:8080/api/v1/rooms/${ROOM_ID}/players" \
  -H 'Content-Type: application/json' \
  -d '{"playerId":"p2","displayName":"Bob"}'
```

Query the room:

```bash
curl -fsS "http://localhost:8080/api/v1/rooms/${ROOM_ID}"
```

Leave and close the room:

```bash
curl -fsS -X DELETE "http://localhost:8080/api/v1/rooms/${ROOM_ID}/players/p2"
curl -fsS -X DELETE "http://localhost:8080/api/v1/rooms/${ROOM_ID}"
```

Responses use `code`, `message`, and `data`. Capacity, duplicate player, and missing room errors are handled by core and the starter.

## Generic WebSocket

The transport registers `/ws/rooms`:

```text
ws://localhost:8080/ws/rooms?roomId={ROOM_ID}
```

Send a heartbeat:

```json
{"type":"ping"}
```

The server returns:

```json
{"type":"pong","timestamp":"2026-09-25T00:00:00Z"}
```

Other JSON messages are wrapped and broadcast to the same room. Connections without `roomId` use the global group.

This endpoint validates transport only. It does not provide player authentication or a game protocol. Implement those in your business WebSocket handler as shown by the Party demo.

## Verification Flow

1. Start the application or Docker Compose.
2. Create a room and record its ID.
3. Join two players.
4. Query the room and verify its members and status.
5. Connect a WebSocket client to `/ws/rooms` and send `ping`.
6. Open two connections for the same room and verify room-scoped broadcasting.
7. Remove a player and close the room.
