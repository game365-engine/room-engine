# room-engine

`room-engine` is a foundational multiplayer game room engine for Java/Spring teams. It provides room lifecycle management, player management, JSON WebSocket transport, and Spring Boot integration.

> ⚠️ WebSocket connections are not authenticated by default. Do not deploy this configuration directly to the public internet in production. See the Important Notes section below.

## Recommended Workflow

1. Read the [Getting Started Guide](docs/getting-started.md).
2. Start `examples/basic-validation-demo` to verify that Java, Maven, and Docker are correctly configured on your machine.
3. Read the [Architecture Guide](docs/architecture.md) to understand module boundaries and dependencies.
4. Copy or refer to `examples/discord-party-turn-demo`, and replace its Party game rules and protocol with your own.
5. Consult the [API Reference](docs/api-reference.md) when integrating the room REST API, WebSocket transport, and starter.

## Directory Structure

```text
room-engine/
├── room-engine-core/
├── room-engine-api/
├── room-engine-transport-websocket/
├── room-engine-spring-boot-starter/
├── examples/
│   ├── basic-validation-demo/
│   └── discord-party-turn-demo/
├── docs/
└── README.md
```

## Module Overview

- `room-engine-core`: Pure room domain model, lifecycle management, player management, events, and persistence interfaces.
- `room-engine-api`: Unified HTTP response model and basic exception handling.
- `room-engine-transport-websocket`: JSON WebSocket encoding/decoding, connection management, authentication hooks, and broadcasting.
- `room-engine-spring-boot-starter`: Automatically provides `RoomService`, API exception handling, and basic WebSocket configuration.
- `examples/basic-validation-demo`: Minimal application for validating the core capabilities; it is not a game-specific template.
- `examples/discord-party-turn-demo`: Complete business reference project demonstrating a turn-based game and a TypeScript client.

## Quick Start

```bash
mvn -pl examples/basic-validation-demo -am test
mvn -f examples/basic-validation-demo/pom.xml spring-boot:run
```

The default service address is `http://localhost:8080`. See the [Getting Started Guide](docs/getting-started.md) for complete commands and the Docker workflow.

## ⚠️ Important Notes

The WebSocket connections provided by this project are **not authenticated by default**. Any client can connect directly and join a room.
**Do not deploy to a public production environment without adding authentication logic.**

### Limitations

- The current version only includes room lifecycle management and a basic WebSocket transport layer. It is intended for learning, prototyping, and use as a reference for further development.
- It does not include: concrete game rule implementations, an account system, a reconnect state machine, server-authoritative validation (anti-cheat), state persistence, or a client SDK.
- For production use, you must add capabilities such as authentication and persistence yourself.

### Production Components (Pro)

The following capabilities have been implemented and validated in a private repository and are available through a one-time license:

- Reconnect state machine, including heartbeat detection, state freezing and recovery, and timeout fallback strategies.
- Server-authoritative validation framework for anti-cheat, including reference rules for voting and quiz-style games.
- State persistence adapter based on Redis, supporting restarts and scaling without losing state.
- Discord Activity reference example and client SDK.

If you need these capabilities, please contact the author through [Issues](link) / [email](link).

## Verification

```bash
mvn test
```

Type-check the TypeScript SDK for the Party demo:

```bash
cd examples/discord-party-turn-demo/client-sdk-ts
npm install
npm run typecheck
```



