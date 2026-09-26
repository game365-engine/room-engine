# Discord Party Turn Demo

This is a Discord Activities party and turn-based game reference built on the basic multiplayer capabilities of `room-engine`.

The first phase does not require real Discord OAuth. HTTP creates and joins rooms, while WebSocket drives the turn-based game.

## Requirements

- Java 25+
- Maven 3.9+
- Node.js 20+ (for the TypeScript SDK)

## Start

From the repository root:

```bash
mvn -pl examples/discord-party-turn-demo -am install -DskipTests
mvn -f examples/discord-party-turn-demo/pom.xml spring-boot:run
```

The default port is `8081`:

```bash
PORT=8082 mvn -f examples/discord-party-turn-demo/pom.xml spring-boot:run
```

Or use Docker Compose:

```bash
docker compose -f examples/discord-party-turn-demo/docker-compose.yml up --build
```

## Create and Join a Room

```bash
ROOM_ID=$(curl -fsS -X POST \
  'http://localhost:8081/api/v1/party/rooms?capacity=4' \
  | sed -n 's/.*"roomId":"\([^"]*\)".*/\1/p')
echo "$ROOM_ID"
```

Join two players:

```bash
curl -fsS -X POST "http://localhost:8081/api/v1/party/rooms/${ROOM_ID}/players" \
  -H 'Content-Type: application/json' \
  -d '{"playerId":"p1","displayName":"Alice"}'

curl -fsS -X POST "http://localhost:8081/api/v1/party/rooms/${ROOM_ID}/players" \
  -H 'Content-Type: application/json' \
  -d '{"playerId":"p2","displayName":"Bob"}'
```

Query, leave, and close:

```bash
curl -fsS "http://localhost:8081/api/v1/party/rooms/${ROOM_ID}"
curl -fsS -X DELETE "http://localhost:8081/api/v1/party/rooms/${ROOM_ID}/players/p2"
curl -fsS -X DELETE "http://localhost:8081/api/v1/party/rooms/${ROOM_ID}"
```

The leave and close endpoints also support the compatibility paths `/api/v1/rooms/...`.

## WebSocket Protocol

Connect with a room and player:

```text
ws://localhost:8081/ws/party?roomId={ROOM_ID}&playerId=p1
```

The server validates membership and immediately sends the current `game_state`.

Client commands:

```json
{"type":"ping"}
{"type":"ready"}
{"type":"start_game"}
{"type":"turn_action","action":"roll"}
{"type":"turn_action","action":"pass"}
```

Rules:

- At least two players are required.
- The first player to join is the host.
- Every player must be ready before the host can start.
- The game lasts three rounds, with one action per player per round.
- `roll` earns 1-6 random points; `pass` earns zero.
- Only `currentPlayerId` may take a turn.
- The server broadcasts a winner when the game finishes.

## TypeScript SDK

The SDK is in `client-sdk-ts` and provides connection, exponential-backoff reconnect, heartbeat, ready, start-game, and turn actions:

```bash
cd examples/discord-party-turn-demo/client-sdk-ts
npm install
npm run typecheck
```

Example:

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

## Verification Flow

1. Start the backend and create a room.
2. Join `p1` and `p2`.
3. Connect two clients to the same room.
4. Send `ready` from both clients.
5. Send `start_game` from `p1`.
6. Take turns according to `currentPlayerId`.
7. Confirm all clients receive the same `game_state`.
8. Try an out-of-turn action and expect `NOT_YOUR_TURN`.
9. Complete three rounds and confirm `FINISHED` and `winner`.
