# Architecture

## 总体结构

项目采用 Maven 多模块结构，但最终运行形态是一个由业务项目组装出来的 Spring Boot 应用。

```text
业务应用或 examples demo
        │
        └── room-engine-spring-boot-starter
              ├── room-engine-api
              ├── room-engine-transport-websocket
              └── room-engine-core
```

## 模块职责

### `room-engine-core`

基础领域模块，不依赖具体游戏规则和 Spring Web 应用：

- `Room`、`Player`、`RoomStatus`；
- 创建、加入、离开、关闭房间；
- 房间容量和玩家合法性校验；
- `RoomEvent` / `RoomEventListener`；
- `PersistenceAdapter` 和内存实现；
- `RoomException` 和基础错误码。

业务模块应该调用 `RoomService`，而不是重新维护房间生命周期。

### `room-engine-api`

HTTP/API 适配层：

- `Result<T>` 统一响应格式；
- `ApiExceptionHandler` 将 `RoomException` 转为 HTTP JSON 响应。

它不定义具体业务 Controller。

### `room-engine-transport-websocket`

通用 WebSocket 传输层：

- `MessageCodec` 和 Jackson JSON 实现；
- `ConnectionAuthenticator` 鉴权扩展点；
- WebSocket 生命周期管理；
- ping/pong；
- 按 `roomId` 分组和广播；
- 通用 `/ws/rooms` 端点。

它不理解 Party 的 `ready`、`turn_action` 等业务命令。

### `room-engine-spring-boot-starter`

Spring Boot 自动配置层：

- 自动注册 `RoomService`；
- 自动加载 API 异常处理；
- 自动加载 WebSocket 基础配置和默认 codec；
- 允许业务应用通过自己的 Bean 覆盖默认实现。

用户业务项目通常只需要依赖这个模块。

### `examples/basic-validation-demo`

最小可运行验证应用：

- 提供通用房间 REST API；
- 验证 starter 自动配置；
- 验证通用 `/ws/rooms`；
- 提供 Docker Compose 启动方式。

它不包含具体游戏规则。

### `examples/discord-party-turn-demo`

完整业务参考应用：

- Party 游戏状态机；
- Ready、房主、回合、分数和胜者规则；
- Party HTTP API；
- Party WebSocket 消息协议；
- TypeScript 客户端 SDK。

其中的游戏状态和协议属于示例业务，不应迁移到 `room-engine-core`。

## 状态边界

基础房间状态由 `RoomService` 管理：

```text
roomId
capacity
room status
players
player id
display name
joinedAt
```

具体游戏状态由业务服务管理：

```text
phase
ready
score
round
current player
last action
winner
```

业务状态可以投影房间玩家信息用于响应，但不能再实现一套独立的房间加入、容量和关闭逻辑。

## 典型启动关系

```text
Spring Boot Application
    │
    ├── starter auto-configuration
    │     ├── RoomService
    │     ├── API exception handling
    │     └── WebSocket transport
    │
    ├── application Controller
    └── application GameService / WebSocket Handler
```

`basic-validation-demo` 验证通用部分，`discord-party-turn-demo` 验证业务扩展方式。

## 可替换扩展点

- `PersistenceAdapter`：替换房间内存存储；
- `ConnectionAuthenticator`：接入登录、Discord OAuth 或自己的 token；
- `MessageCodec`：替换消息编解码；
- `ValidationRule`：实现服务端权威规则校验；
- `ReconnectPolicy`：实现断线重连和状态恢复策略。

这些接口位于基础模块中，但具体策略应由业务应用或额外模块提供。
