# AgentScope 聊天应用

这是一个基于 AgentScope 和 Spring Boot 的 AI 聊天应用，支持 Web 界面交互。

## 项目结构

```
src/main/java/com/lhj/rush/
├── RushApplication.java                 # Spring Boot 主启动类
├── config/
│   ├── ModelConfig.java              # 模型配置类
│   ├── AgentConfig.java              # Agent 配置类
│   ├── WorkspaceConfig.java          # 工作区配置类
│   └── CompactionConfig.java         # 压缩配置类
├── controller/
│   └── ChatController.java           # 聊天控制器
├── service/
│   └── AgentService.java             # Agent 服务层
└── dto/
    ├── ChatRequest.java              # 聊天请求 DTO
    └── ChatResponse.java             # 聊天响应 DTO

src/main/resources/
├── application.yml                    # 应用配置文件
└── templates/
    └── chat.html                      # 前端聊天页面
```

## 快速开始

### 1. 配置 API Key

编辑 `src/main/resources/application.yml` 文件，设置你的 DeepSeek API Key：

```yaml
agentscope:
  model:
    api-key: your-api-key-here
```

### 2. 运行应用

#### 方式一：使用 Maven
```bash
mvn spring-boot:run
```

#### 方式二：打包后运行
```bash
mvn clean package
java -jar target/rush-java-0.0.1-SNAPSHOT.jar
```

### 3. 访问应用

打开浏览器访问：http://localhost:8080

## 功能特性

### ✅ 已实现功能

1. **配置化管理**
   - 所有配置项集中在 `application.yml`
   - 通过配置类自动注入
   - 支持运行时修改配置

2. **Web 聊天界面**
   - 美观的渐变色 UI 设计
   - **WebFlux SSE 流式响应**（基于 Project Reactor）
   - 实时消息逐字显示效果
   - 会话状态保持
   - 错误提示处理

3. **REST API (WebFlux)**
   - POST `/api/chat/stream` - 响应式 SSE 流式聊天接口
   - 基于 `Flux<ServerSentEvent>` 实现
   - 异步非阻塞处理
   - 支持自定义会话 ID 和用户 ID
   - 事件类型：session, start, message, complete, error

4. **会话管理**
   - 自动会话 ID 生成
   - 会话上下文缓存
   - 多用户支持

5. **AgentScope 集成**
   - OpenAI 兼容接口
   - 工作区自动初始化
   - 对话压缩功能
   - 持久化会话状态

### 🚀 WebFlux 优势

- **响应式编程**: 使用 Flux 实现声明式流处理
- **非阻塞 I/O**: Netty 服务器提供高性能并发
- **背压支持**: 自动处理流量控制
- **资源效率**: 更少的线程消耗，更高的吞吐量
- **优雅简洁**: 链式 API，代码更清晰

## API 使用示例

### SSE 流式聊天请求

```bash
curl -N -X POST http://localhost:8080/api/chat/stream \
  -H "Content-Type: application/json" \
  -d '{
    "message": "你好，请介绍一下自己",
    "sessionId": "my-session-123",
    "userId": "user-456"
  }'
```

### SSE 事件格式

服务器会发送以下类型的事件：

1. **session**: 会话 ID
   ```
   event: session
   data: uuid-here
   ```

2. **start**: 开始处理
   ```
   event: start
   data: 开始处理...
   ```

3. **message**: 消息内容（分块发送）
   ```
   event: message
   data: 这是回复的一部分内容
   ```

4. **complete**: 完成
   ```
   event: complete
   data: 完成
   ```

5. **error**: 错误信息
   ```
   event: error
   data: 错误描述
   ```

### 前端使用 EventSource

```javascript
fetch('/api/chat/stream', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ message: '你好' })
})
.then(response => {
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    // 处理流式数据...
});
```

## 配置说明

### 模型配置 (agentscope.model)

| 参数 | 说明 | 默认值 |
|------|------|--------|
| base-url | API 基础地址 | https://api.deepseek.com/v1 |
| api-key | API Key | 必填 |
| model-name | 模型名称 | deepseek-chat |
| stream | 是否启用流式输出 | true |

### Agent 配置 (agentscope.agent)

| 参数 | 说明 | 默认值 |
|------|------|--------|
| name | Agent 名称 | quickstart-agent |
| sys-prompt | 系统提示词 | 你是一个帮助用户做笔记的助手。 |

### 工作区配置 (agentscope.workspace)

| 参数 | 说明 | 默认值 |
|------|------|--------|
| path | 工作区路径 | .agentscope/workspace |

### 压缩配置 (agentscope.compaction)

| 参数 | 说明 | 默认值 |
|------|------|--------|
| trigger-messages | 触发压缩的消息数 | 30 |
| keep-messages | 压缩后保留的消息数 | 10 |
| flush-before-compact | 压缩前刷新事实 | true |

## 技术栈

- **后端框架**: Spring Boot 2.7.6
- **响应式框架**: Spring WebFlux (Project Reactor)
- **AI 框架**: AgentScope Harness 1.1.0-RC1
- **模板引擎**: Thymeleaf
- **通信协议**: SSE (Server-Sent Events) via WebFlux Flux
- **服务器**: Netty (响应式非阻塞服务器)
- **构建工具**: Maven
- **Java 版本**: 17

## 注意事项

1. **API Key 安全**: 请勿将包含真实 API Key 的代码提交到公共仓库
2. **会话存储**: 当前使用内存存储会话，生产环境建议使用 Redis
3. **模型选择**: 推荐使用 `deepseek-chat` 模型，其他模型可能不兼容
4. **网络要求**: 需要能够访问 DeepSeek API 服务器

## 常见问题

### Q: 出现 HTTP 404 错误？
A: 检查以下几点：
- 确保使用 `OpenAIChatModel` 而不是 `DashScopeChatModel`
- 确认 baseUrl 设置为 `https://api.deepseek.com/v1`
- 确认 model-name 为 `deepseek-chat`

### Q: 如何更换其他 AI 服务商？
A: 修改 `application.yml` 中的配置：
```yaml
agentscope:
  model:
    base-url: https://your-api-endpoint/v1
    api-key: your-api-key
    model-name: your-model-name
```

### Q: 如何自定义系统提示词？
A: 修改 `application.yml` 中的 `agentscope.agent.sys-prompt` 配置项。

## 许可证

MIT License
