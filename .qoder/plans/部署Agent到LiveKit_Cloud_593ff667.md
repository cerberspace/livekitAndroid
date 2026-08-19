# 部署 Agent 到 LiveKit Cloud（两阶段方案）

## 第一阶段：Agent Builder 快速验证

目标：在浏览器中零代码创建 Agent，验证 Android 端到端语音对话连通性。

### Task 1: 在 LiveKit Cloud 创建 Agent

用户手动操作（无需代码改动）：

1. 登录 https://cloud.livekit.io，进入你的项目
2. 导航到 **Agents** 面板，点击 **Deploy new agent**
3. 在 Agent Builder 中配置：
   - **Agent name**: 设为 `voxai-agent`（用于后续 dispatch）
   - **Instructions**: 填入中文系统提示词（如"你是名叫 VoxAI 的智能语音助手，用中文与用户自然对话..."）
   - **Models**: 选择 LiveKit Inference 内置模型（STT: Deepgram Nova-3, LLM: OpenAI, TTS: Cartesia Sonic-3）
   - **Welcome greeting**: 启用并设置中文问候
4. 点击 **Deploy agent** 部署到 LiveKit Cloud

### Task 2: 修改 Android 端 Token 支持 Agent Dispatch

Android 端需要在 JWT token 中加入 agent dispatch 配置，使得用户加入房间时自动触发 Agent 加入。

文件: `android/app/src/main/java/com/voxai/app/data/TokenGenerator.kt`

当前 token 只包含 `video` grant，需要添加 `roomConfig.agents` 字段。由于 LiveKit 的 agent dispatch 使用 protobuf 格式，需要手动在 payload 中构造对应的 JSON 结构：

```kotlin
// 在 payload 中添加 roomConfig
val agentDispatch = JsonObject().apply {
    addProperty("agentName", "voxai-agent")  // 与 Agent Builder 中的名称一致
}
val roomConfig = JsonObject().apply {
    add("agents", JsonArray().apply { add(agentDispatch) })
}
payload.add("roomConfig", roomConfig)
```

注意：此字段仅在房间首次创建时生效。每次通话使用不同的 roomName 即可确保 dispatch 触发。

### Task 3: 验证连通性

1. 在 Agent Builder 中点击 **Launch Console** 可以先在浏览器中测试 Agent
2. 运行 Android App，配置 LiveKit Cloud 的 URL/Key/Secret
3. 发起通话 -> Android 用户加入房间 -> Agent 自动被 dispatch 到同一房间
4. 验证：能听到 Agent 问候语、能进行语音对话

**已知限制**（Agent Builder 阶段）：
- 不会有实时字幕（Agent Builder 不发送自定义 data channel 消息）
- 使用海外服务商模型（Deepgram/OpenAI/Cartesia），非豆包/千问
- 不支持自定义 data channel 协议

---

## 第二阶段：自定义代码集成豆包/千问（后续）

验证通过后，修改 `agent/agent.py` 集成火山引擎 + 千问，通过 CLI 部署。

### Task 4: 更新 requirements.txt

文件: `agent/requirements.txt`

```
livekit-agents[openai,silero]>=1.0.0
livekit-plugins-volcengine>=1.3.0
python-dotenv>=1.0.0
```

### Task 5: 修改 agent.py 集成火山引擎和千问

文件: `agent/agent.py`

- **STT**: `volcengine.STT(app_id, cluster, access_token)` — 环境变量 `VOLCENGINE_APP_ID`, `VOLCENGINE_CLUSTER`, `VOLCENGINE_STT_ACCESS_TOKEN`
- **TTS**: `volcengine.TTS(app_id, cluster, access_token, voice)` — 环境变量 `VOLCENGINE_TTS_ACCESS_TOKEN`, `VOLCENGINE_TTS_VOICE`
- **LLM**: `openai.LLM(model, api_key, base_url)` 连接千问 DashScope — `DASHSCOPE_API_KEY`, base_url: `https://dashscope.aliyuncs.com/compatible-mode/v1`

### Task 6: 创建部署文件

- `agent/Dockerfile` — LiveKit 官方 pip 模板
- `agent/.dockerignore` — 排除无关文件
- `agent/.env.example` — 更新环境变量模板

### Task 7: CLI 部署

```bash
cd agent
lk cloud auth
lk agent create
lk agent update-secrets --secrets "VOLCENGINE_APP_ID=xxx" ...
```

---

## 当前执行范围

本次只执行 **第一阶段 Task 1-3**，即 Agent Builder 验证 + Android 端 token dispatch 修改。

| 文件 | 操作 |
|------|------|
| `android/app/src/main/java/com/voxai/app/data/TokenGenerator.kt` | 修改（添加 agent dispatch） |
