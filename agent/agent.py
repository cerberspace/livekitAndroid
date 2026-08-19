"""
VoxAI — LiveKit 实时语音助手 Agent

基于 LiveKit Agents 框架构建的 AI 语音助手后端。
支持可配置的 STT / LLM / TTS 服务商，并通过数据通道实时推送字幕到客户端。

使用方法：
  1. 复制 .env.example 为 .env 并填入 API 密钥
  2. pip install -r requirements.txt
  3. python agent.py start

环境变量说明见 .env.example
"""

from __future__ import annotations

import json
import os
import asyncio
import logging
from typing import Any

from dotenv import load_dotenv
from livekit import agents
from livekit.agents import (
    Agent,
    AgentSession,
    JobContext,
    WorkerOptions,
    cli,
)
from livekit.agents.voice import room_io
from livekit.plugins import (
    cartesia,
    deepgram,
    openai,
    silero,
)

load_dotenv()

logger = logging.getLogger("voxai-agent")

# ---------------------------------------------------------------------------
# Data channel protocol — sent to the Android client via room data messages
# ---------------------------------------------------------------------------

DATA_TOPIC = "voxai"

# Message types
MSG_INTERIM_TRANSCRIPT = "interim_transcript"   # 用户实时语音识别（中间结果）
MSG_FINAL_TRANSCRIPT = "final_transcript"       # 最终字幕（用户或AI）
MSG_AGENT_STATE = "agent_state"                  # Agent 状态变化
MSG_CALL_META = "call_meta"                      # 通话元数据


def _publish(ctx: JobContext, message: dict[str, Any]) -> None:
    """Publish a JSON data message to all room participants."""
    payload = json.dumps(message, ensure_ascii=False).encode("utf-8")
    asyncio.create_task(
        ctx.room.local_participant.publish_data(
            payload=payload,
            reliable=True,
            topic=DATA_TOPIC,
        )
    )


# ---------------------------------------------------------------------------
# Provider configuration — reads from environment variables
# ---------------------------------------------------------------------------

def _build_stt():
    """Build the Speech-to-Text provider."""
    provider = os.getenv("STT_PROVIDER", "deepgram").lower()

    if provider == "deepgram":
        return deepgram.STT(
            model=os.getenv("DEEPGRAM_STT_MODEL", "nova-3"),
            language=os.getenv("DEEPGRAM_STT_LANGUAGE", "multi"),
            api_key=os.getenv("DEEPGRAM_API_KEY"),
            smart_format=True,
            interim_results=True,
        )
    elif provider == "openai":
        # OpenAI Realtime STT (via realtime API)
        return openai.STT(
            model=os.getenv("OPENAI_STT_MODEL", "whisper-1"),
            api_key=os.getenv("OPENAI_API_KEY"),
        )
    else:
        raise ValueError(f"Unknown STT provider: {provider}")


def _build_llm():
    """Build the Large Language Model provider."""
    provider = os.getenv("LLM_PROVIDER", "openai").lower()

    if provider == "openai":
        return openai.LLM(
            model=os.getenv("OPENAI_LLM_MODEL", "gpt-4o-mini"),
            api_key=os.getenv("OPENAI_API_KEY"),
            base_url=os.getenv("OPENAI_BASE_URL"),  # 支持 OpenAI 兼容API（如 Azure、代理）
        )
    elif provider == "ollama":
        # 本地 Ollama — OpenAI 兼容接口
        return openai.LLM.with_ollama(
            model=os.getenv("OLLAMA_MODEL", "qwen2.5:latest"),
            base_url=os.getenv("OLLAMA_BASE_URL", "http://localhost:11434/v1"),
        )
    else:
        raise ValueError(f"Unknown LLM provider: {provider}")


def _build_tts():
    """Build the Text-to-Speech provider."""
    provider = os.getenv("TTS_PROVIDER", "cartesia").lower()

    if provider == "cartesia":
        return cartesia.TTS(
            model=os.getenv("CARTESIA_TTS_MODEL", "sonic-2"),
            voice=os.getenv("CARTESIA_VOICE_ID", "f9836c6e-2bd7-4a12-aa56-69c6f692e4b4"),
            api_key=os.getenv("CARTESIA_API_KEY"),
        )
    elif provider == "deepgram":
        return deepgram.TTS(
            model=os.getenv("DEEPGRAM_TTS_MODEL", "aura-2-asteria-en"),
            api_key=os.getenv("DEEPGRAM_API_KEY"),
        )
    elif provider == "openai":
        return openai.TTS(
            model=os.getenv("OPENAI_TTS_MODEL", "tts-1"),
            voice=os.getenv("OPENAI_TTS_VOICE", "alloy"),
            api_key=os.getenv("OPENAI_API_KEY"),
        )
    else:
        raise ValueError(f"Unknown TTS provider: {provider}")


def _build_vad():
    """Build the Voice Activity Detector."""
    return silero.VAD.load()


# ---------------------------------------------------------------------------
# Agent definition
# ---------------------------------------------------------------------------

SYSTEM_PROMPT = os.getenv(
    "AGENT_SYSTEM_PROMPT",
    """你是一个名叫 VoxAI 的智能语音助手。你用中文与用户进行自然流畅的语音对话。

你的特点：
- 回答简洁明了，适合语音交互（每次回复不超过 2-3 句话）
- 友好、热情，有幽默感
- 当不确定时会诚实地说不知道
- 可以帮助用户处理日常问题、安排日程、解答疑问
""",
)


class VoxAIAgent(Agent):
    """The voice AI assistant agent."""

    def __init__(self) -> None:
        super().__init__(
            instructions=SYSTEM_PROMPT,
        )

    async def on_enter(self) -> None:
        """Called when the agent enters the session."""
        logger.info("VoxAI agent entered the room")
        # Generate a greeting when entering
        await self.session.generate_reply(
            instructions="向用户打招呼，介绍自己是 VoxAI 语音助手，询问有什么可以帮忙的。简短友好。"
        )


# ---------------------------------------------------------------------------
# Entrypoint
# ---------------------------------------------------------------------------

async def entrypoint(ctx: JobContext) -> None:
    """Main agent entrypoint — called when a job is dispatched."""
    logger.info("Connecting to LiveKit room: %s", ctx.room.name)

    await ctx.connect()

    # Build the voice session with configured providers
    session = AgentSession(
        stt=_build_stt(),
        llm=_build_llm(),
        tts=_build_tts(),
    )

    # --- Wire up event handlers for subtitle data channel ---

    @session.on("user_input_transcribed")
    def on_user_transcribed(ev) -> None:
        """Send interim and final user transcripts to the client."""
        _publish(ctx, {
            "type": MSG_INTERIM_TRANSCRIPT,
            "speaker": "user",
            "text": ev.transcript,
            "is_final": ev.is_final,
            "timestamp": ev.created_at,
        })
        if ev.is_final:
            logger.debug("User said: %s", ev.transcript)

    @session.on("conversation_item_added")
    def on_conversation_item(ev) -> None:
        """Send final transcripts (both user and agent) to the client."""
        item = ev.item
        # Only handle ChatMessage items (not function calls or handoffs)
        if not hasattr(item, "role") or not hasattr(item, "text_content"):
            return

        text = item.text_content
        if not text:
            return

        speaker = "agent" if item.role == "assistant" else "user"
        _publish(ctx, {
            "type": MSG_FINAL_TRANSCRIPT,
            "speaker": speaker,
            "text": text,
            "id": item.id,
            "timestamp": ev.created_at,
        })
        logger.debug("[%s] %s", speaker, text)

    @session.on("agent_state_changed")
    def on_agent_state_changed(ev) -> None:
        """Notify the client of agent state changes (listening/thinking/speaking)."""
        _publish(ctx, {
            "type": MSG_AGENT_STATE,
            "agent_state": ev.new_state,
            "user_state": session.user_state,
            "timestamp": ev.created_at,
        })

    @session.on("user_state_changed")
    def on_user_state_changed(ev) -> None:
        """Notify the client of user state changes (listening/speaking)."""
        _publish(ctx, {
            "type": MSG_AGENT_STATE,
            "agent_state": session.agent_state,
            "user_state": ev.new_state,
            "timestamp": ev.created_at,
        })

    # --- Start the session ---
    await session.start(
        agent=VoxAIAgent(),
        room=ctx.room,
    )

    logger.info("VoxAI session started in room %s", ctx.room.name)


if __name__ == "__main__":
    cli.run_app(WorkerOptions(entrypoint_fnc=entrypoint))
