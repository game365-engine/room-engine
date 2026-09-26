# Architecture

## Overview

The project uses a Maven multi-module layout. A business project assembles the modules into one Spring Boot application at runtime.

```text
Business application or example
        |
        +-- room-engine-spring-boot-starter
              +-- room-engine-api
              +-- room-engine-transport-websocket
              +-- room-engine-core
```

## Module Responsibilities

### `room-engine-core`

The framework-agnostic domain module:

- `Room`, `Player`, and `RoomStatus`;
- create, join, leave, and close operations;
- capacity and player validation;
- `RoomEvent` and `RoomEventListener`;
- `PersistenceAdapter` and its in-memory implementation;
- `RoomException` and base error codes.

Business modules should call `RoomService` instead of implementing another room lifecycle.

### `room-engine-api`

HTTP/API adapter layer:

- `Result<T>` for a common response format;
- `ApiExceptionHandler` for mapping `RoomException` to JSON responses.

It does not define business controllers.

### `room-engine-transport-websocket`

Generic WebSocket transport:

- `MessageCodec` and the Jackson JSON implementation;
- `ConnectionAuthenticator` extension point;
- WebSocket lifecycle management;
- ping/pong;
- `roomId` grouping and broadcasting;
- generic `/ws/rooms` endpoint.

It does not understand Party commands such as `ready` or `turn_action`.

### `room-engine-spring-boot-starter`

Spring Boot auto-configuration:

- registers `RoomService`;
- loads API exception handling;
- loads WebSocket infrastructure and the default codec;
- allows business applications to override default beans.

Most business applications only need this dependency.

### `examples/basic-validation-demo`

Minimal runnable validation application:

- exposes generic room REST APIs;
- verifies starter auto-configuration;
- verifies generic `/ws/rooms`;
- includes Docker Compose startup.

It contains no game rules.

### `examples/discord-party-turn-demo`

Complete business reference application:

- Party game state machine;
- ready, host, turn, score, and winner rules;
- Party HTTP API;
- Party WebSocket protocol;
- TypeScript client SDK.

Its game state and protocol are example business code and should not be moved into `room-engine-core`.

## State Boundaries

`RoomService` owns base room state:

```text
roomId
capacity
room status
players
player id
display name
joinedAt
```

The business service owns game state:

```text
phase
ready
score
round
current player
last action
winner
```

Business responses may project room player data, but business code must not implement a second room join, capacity, or close flow.

## Typical Startup Composition

```text
Spring Boot Application
    |
    +-- starter auto-configuration
    |     +-- RoomService
    |     +-- API exception handling
    |     +-- WebSocket transport
    |
    +-- application Controller
    +-- application GameService / WebSocket Handler
```

`basic-validation-demo` validates the generic stack; `discord-party-turn-demo` validates business extensions.

## Extension Points

- `PersistenceAdapter`: replace in-memory room storage;
- `ConnectionAuthenticator`: integrate login, Discord OAuth, or your own token;
- `MessageCodec`: replace message encoding;
- `ValidationRule`: implement authoritative server-side validation;
- `ReconnectPolicy`: implement reconnect and state recovery.

These interfaces live in the base modules, while concrete policies belong to the business application or an additional module.
