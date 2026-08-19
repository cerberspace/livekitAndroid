# VoxAI — LiveKit 实时语音助手

基于 LiveKit Agent 构建的实时 AI 语音通话 Android 应用。

## 架构

```
┌───────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│   Android App     │       │  LiveKit Server  │       │  Agent Server     │
│  (Kotlin/Compose) │◄─────►│     (SFU)       │◄─────►│   (Python)        │
│                   │       │                  │       │                   │
│ • 麦克风采集       │──────►│                  │──────►│ Deepgram STT      │
│ • 音频播放         │◄──────│                  │◄──────│ OpenAI LLM        │
│ • 实时字幕         │◄──────│  数据通道        │◄──────│ Cartesia TTS      │
│ • 通话历史         │       │                  │       │ 字幕推送           │
└───────────────────┘       └──────────────────┘       └──────────────────┘
```

## 项目结构

```
livekitAndroid/
├── design-prototype/     # Web 视觉原型（React + Tailwind）
├── agent/                # Python LiveKit Agent 后端
│   ├── agent.py          # 主 Agent 文件
│   ├── requirements.txt  # Python 依赖
│   └── .env.example      # 环境变量模板
└── android/              # Android 客户端
    └── app/src/main/java/com/voxai/app/
        ├── data/          # 数据层
        │   ├── LiveKitManager.kt    # LiveKit 连接管理
        │   ├── DataMessage.kt       # 数据通道消息协议
        │   ├── TokenGenerator.kt    # JWT Token 生成
        │   ├── AppPreferences.kt    # 设置存储
        │   └── CallHistoryDao.kt    # Room 数据库
        ├── ui/
        │   ├── theme/     # 暗黑霓虹风设计系统
        │   ├── screens/  # 三个页面
        │   │   ├── ConnectScreen.kt   # 连接页
        │   │   ├── CallScreen.kt      # 通话页
        │   │   └── HistoryScreen.kt   # 历史页
        │   ├── components/ # 可复用组件
        │   └── navigation/ # 导航
        └── viewmodel/    # ViewModel
```

## 快速开始

### 1. 后端 Agent

```bash
cd agent
python3.11 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt

# 配置环境变量
cp .env.example .env
# 编辑 .env 填入 LiveKit 和 AI 服务商密钥

# 启动 Agent
python agent.py start
```

### 2. Android APP

```bash
cd android
# 编译
./gradlew assembleDebug

# APK 输出
# app/build/outputs/apk/debug/app-debug.apk
```

安装到设备后：
1. 打开 App → 点击设置图标
2. 填入 LiveKit 服务器 URL、API Key、API Secretx(需要自己注册 https://cloud.livekit.io 创建project获取)
3. 返回 → 输入房间名和昵称 → 点击「开始语音通话」

## 功能

- **AI 语音对话** — 通过 LiveKit Agent 实现实时 AI 语音交互
- **实时字幕** — STT 识别结果和 AI 回复实时显示为字幕
- **通话历史** — Room 数据库持久化通话记录和对话内容
- **暗黑霓虹风 UI** — 玻璃拟态卡片、霓虹渐变、音频波形动画

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Python · LiveKit Agents · Deepgram STT · OpenAI LLM · Cartesia TTS |
| Android | Kotlin · Jetpack Compose · Material 3 · LiveKit Android SDK |
| 数据 | Room 数据库 · SharedPreferences · Gson |

## 环境变量

见 `agent/.env.example`，支持配置：

- **STT_PROVIDER**: `deepgram` / `openai`
- **LLM_PROVIDER**: `openai` / `ollama`
- **TTS_PROVIDER**: `cartesia` / `deepgram` / `openai`
