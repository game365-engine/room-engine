# Getting Started

This guide is for users running `room-engine` after downloading it from itch.io.

## 1. Requirements

- Java 25+
- Maven 3.9+
- Docker Desktop or a Docker installation with `docker compose` (optional)
- Node.js 20+ (only required for the Party demo TypeScript SDK)

Check the tools from the repository root:

```bash
java -version
mvn -version
docker compose version
```

## 2. Run the Basic Validation Demo

The basic validation demo verifies the starter, room REST API, and generic WebSocket transport:

```bash
mvn -pl examples/basic-validation-demo -am install -DskipTests
mvn -f examples/basic-validation-demo/pom.xml spring-boot:run
```

The default port is `8080`:

```bash
PORT=8082 mvn -f examples/basic-validation-demo/pom.xml spring-boot:run
```

See the [demo README](../examples/basic-validation-demo/README.md) for API examples.

## 3. Use Docker

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

## 4. Verify the Room REST API

Create a room and save its ID:

```bash
ROOM_ID=$(curl -fsS -X POST \
  'http://localhost:8080/api/v1/rooms?capacity=4' \
  | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')
echo "$ROOM_ID"
```

Join a player and query the room:

```bash
curl -fsS -X POST "http://localhost:8080/api/v1/rooms/${ROOM_ID}/players" \
  -H 'Content-Type: application/json' \
  -d '{"playerId":"p1","displayName":"Alice"}'

curl -fsS "http://localhost:8080/api/v1/rooms/${ROOM_ID}"
```

## 5. Build from the Party Demo

After the basic validation succeeds, use `examples/discord-party-turn-demo` as the business reference:

1. Copy its Spring Boot and WebSocket setup.
2. Keep the `room-engine-spring-boot-starter` dependency.
3. Replace `PartyGameService` with your game state machine.
4. Replace the Party messages with your own protocol.
5. Keep `RoomService` responsible for room and player lifecycle.

Start the Party demo:

```bash
mvn -pl examples/discord-party-turn-demo -am install -DskipTests
mvn -f examples/discord-party-turn-demo/pom.xml spring-boot:run
```

## 6. Build and Test

```bash
mvn test
```

Verify the TypeScript SDK:

```bash
cd examples/discord-party-turn-demo/client-sdk-ts
npm install
npm run typecheck
```

## 7. Moving Toward Production

The package is intended for local development and single-instance validation. A production deployment should add at least:

- WebSocket authentication;
- external persistence or room snapshots;
- reconnect and session recovery;
- business-level validation, rate limiting, and logging;
- HTTPS/WSS, reverse proxy, and container deployment configuration.
