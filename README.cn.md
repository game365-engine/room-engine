# room-engine

`room-engine` 是面向 Java/Spring 团队的多人游戏房间引擎基础包，提供房间生命周期、玩家管理、JSON WebSocket 传输和 Spring Boot 集成能力。

> ⚠️ 默认 WebSocket 连接未做身份认证，请勿直接部署到生产环境公网。详见下方 重要说明。

## 推荐使用流程

1. 阅读 [上手指南](docs/getting-started.cn.md)。
2. 启动 `examples/basic-validation-demo`，确认本机 Java、Maven 和 Docker 环境正常。
3. 阅读 [架构说明](docs/architecture.cn.md)，了解模块边界和依赖关系。
4. 复制或参考 `examples/discord-party-turn-demo`，替换其中的 Party 游戏规则和协议。
5. 查阅 [API 参考](docs/api-reference.cn.md)，接入房间 REST API、WebSocket 和 starter。

## 目录结构

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
└── README.cn.md
```

## 模块定位

- `room-engine-core`：纯房间领域模型、生命周期、玩家管理、事件和持久化接口。
- `room-engine-api`：统一 HTTP 响应模型和基础异常处理。
- `room-engine-transport-websocket`：JSON WebSocket 编解码、连接管理、鉴权钩子和广播。
- `room-engine-spring-boot-starter`：自动提供 `RoomService`、API 异常处理和 WebSocket 基础配置。
- `examples/basic-validation-demo`：最小基础能力验证应用，不是具体游戏模板。
- `examples/discord-party-turn-demo`：完整的业务参考项目，演示回合制游戏和 TypeScript 客户端。

## 快速启动

```bash
mvn -pl examples/basic-validation-demo -am test
mvn -f examples/basic-validation-demo/pom.xml spring-boot:run
```

默认服务地址为 `http://localhost:8080`。完整命令和 Docker 流程见 [上手指南](docs/getting-started.cn.md)。


## ⚠️ 重要说明

本项目默认的 WebSocket 连接**未做身份认证**，任何客户端都可以直接连接并加入房间。
**请勿在未补充认证逻辑的情况下直接部署到公网生产环境。**

### Limitations

- 当前版本仅包含房间生命周期管理与基础 WebSocket 传输层，用于学习、原型验证和二次开发参考
- 未包含：具体游戏规则实现、账号系统、断线重连状态机、服务端权威校验（防作弊）、状态持久化、客户端 SDK
- 如需用于生产环境，需要自行补充身份认证、持久化等能力

### 生产级组件（Pro）

以下能力已在私有仓库中实现并经过验证，采用一次性授权方式获取：

- 断线重连状态机（心跳检测、状态冻结与恢复、超时兜底策略）
- 服务端权威校验框架（防作弊，含投票/答题类参考规则）
- 状态持久化适配器（Redis，支持服务重启/扩容不丢状态）
- Discord Activity 参考示例 + 客户端 SDK

如有需要，请通过 [zhangwarren90@gmail.com] 联系作者获取。


## 验证

```bash
mvn test
```

Party demo 的 TypeScript SDK 类型检查：

```bash
cd examples/discord-party-turn-demo/client-sdk-ts
npm install
npm run typecheck
```

