import { useState, useEffect, useRef, useCallback } from 'react'
import { Mic, MicOff, PhoneOff, Volume2, VolumeX } from 'lucide-react'
import { Button } from '@/components/ui/button'
import type { CallSession, TranscriptEntry } from '@/App'

interface CallScreenProps {
  session: CallSession
  onEndCall: (transcript: TranscriptEntry[], duration: number) => void
}

// Simulated agent responses for prototype
const agentResponses = [
  '你好！我是 VoxAI，你的实时语音助手。有什么我可以帮助你的吗？',
  '好的，我明白了。让我想想怎么帮你解决这个问题。',
  '这是一个很好的问题。根据我的理解，你可以从以下几个方面入手。',
  '我已经帮你记录下来了。还有其他需要处理的吗？',
  '没问题！我会持续关注这个事情，有进展随时告诉你。',
]

export default function CallScreen({ session, onEndCall }: CallScreenProps) {
  const [muted, setMuted] = useState(false)
  const [speakerOn, setSpeakerOn] = useState(true)
  const [duration, setDuration] = useState(0)
  const [transcript, setTranscript] = useState<TranscriptEntry[]>([])
  const [currentSubtitle, setCurrentSubtitle] = useState<{ speaker: 'user' | 'agent'; text: string } | null>(null)
  const [isAgentSpeaking, setIsAgentSpeaking] = useState(false)
  const [isUserSpeaking, setIsUserSpeaking] = useState(false)
  const transcriptEndRef = useRef<HTMLDivElement>(null)

  // Timer
  useEffect(() => {
    const interval = setInterval(() => {
      setDuration(d => d + 1)
    }, 1000)
    return () => clearInterval(interval)
  }, [])

  // Simulate conversation flow
  useEffect(() => {
    let step = 0
    const flow = async () => {
      // Agent greeting
      await sleep(1500)
      await speakAgent(agentResponses[0])
      // User "speaks"
      await sleep(800)
      await speakUser('你好，帮我安排一下今天的工作')
      // Agent responds
      await sleep(500)
      await speakAgent(agentResponses[1])
      // User "speaks"
      await sleep(800)
      await speakUser('我想先处理紧急的邮件，然后准备下午的会议')
      // Agent responds
      await sleep(500)
      await speakAgent(agentResponses[2])
      // User "speaks"
      await sleep(800)
      await speakUser('好的，谢谢你')
      // Agent responds
      await sleep(500)
      await speakAgent(agentResponses[3])
    }

    const sleep = (ms: number) => new Promise(r => setTimeout(r, ms))

    async function speakAgent(text: string) {
      setIsAgentSpeaking(true)
      setCurrentSubtitle({ speaker: 'agent', text })
      // Stream text word by word
      const words = text.split('')
      let displayed = ''
      for (let i = 0; i < words.length; i++) {
        displayed += words[i]
        setCurrentSubtitle({ speaker: 'agent', text: displayed })
        await sleep(40)
      }
      await sleep(800)
      setTranscript(prev => [...prev, { id: `${Date.now()}`, speaker: 'agent', text, timestamp: Date.now() }])
      setCurrentSubtitle(null)
      setIsAgentSpeaking(false)
    }

    async function speakUser(text: string) {
      setIsUserSpeaking(true)
      setCurrentSubtitle({ speaker: 'user', text })
      await sleep(1000)
      setTranscript(prev => [...prev, { id: `${Date.now()}`, speaker: 'user', text, timestamp: Date.now() }])
      setCurrentSubtitle(null)
      setIsUserSpeaking(false)
    }

    flow()
  }, [])

  // Auto scroll transcript
  useEffect(() => {
    transcriptEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [transcript, currentSubtitle])

  const handleEndCall = useCallback(() => {
    onEndCall(transcript, duration)
  }, [transcript, duration, onEndCall])

  const formatDuration = (s: number) => {
    const m = Math.floor(s / 60)
    const sec = s % 60
    return `${m.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`
  }

  return (
    <div className="h-full flex flex-col relative overflow-hidden">
      {/* Background gradient */}
      <div className="absolute inset-0 bg-gradient-to-b from-neon-purple/5 via-background to-background" />

      {/* Header */}
      <div className="relative z-10 px-6 pt-16 pb-4">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-xs text-muted-foreground font-mono">{session.roomName}</p>
            <p className="text-2xl font-bold tracking-tight">
              {formatDuration(duration)}
            </p>
          </div>
          <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-emerald-500/10 border border-emerald-500/20">
            <div className="w-2 h-2 rounded-full bg-emerald-400 animate-glow-pulse" />
            <span className="text-xs text-emerald-400 font-medium">已连接</span>
          </div>
        </div>
      </div>

      {/* Agent avatar */}
      <div className="relative z-10 flex flex-col items-center justify-center flex-1 px-6">
        <div className="relative w-40 h-40 mb-6">
          {/* Pulse rings */}
          {(isAgentSpeaking || isUserSpeaking) && (
            <>
              <div className="absolute inset-0 rounded-full bg-neon-cyan/20 animate-pulse-ring" />
              <div className="absolute inset-0 rounded-full bg-neon-purple/20 animate-pulse-ring" style={{ animationDelay: '0.5s' }} />
            </>
          )}
          {/* Glow */}
          <div className={`absolute inset-0 rounded-full blur-2xl transition-all duration-500 ${isAgentSpeaking ? 'bg-neon-cyan/30 scale-110' : 'bg-neon-purple/20'}`} />
          {/* Avatar image */}
          <div className="relative w-40 h-40 rounded-full overflow-hidden neon-border">
            <img src="/images/agent-avatar.png" alt="AI Assistant" className="w-full h-full object-cover" />
          </div>
        </div>

        {/* Agent name & status */}
        <h2 className="text-xl font-bold mb-1">VoxAI 助手</h2>
        <div className="flex items-center gap-2 mb-8">
          <div className={`w-1.5 h-1.5 rounded-full transition-colors ${isAgentSpeaking ? 'bg-neon-cyan animate-glow-pulse' : isUserSpeaking ? 'bg-neon-purple animate-glow-pulse' : 'bg-muted-foreground'}`} />
          <span className="text-xs text-muted-foreground">
            {isAgentSpeaking ? '正在回复...' : isUserSpeaking ? '正在聆听...' : '连接中'}
          </span>
        </div>

        {/* Audio waveform */}
        <div className="flex items-center justify-center gap-1 h-16 mb-2">
          {Array.from({ length: 32 }).map((_, i) => {
            const active = isAgentSpeaking || isUserSpeaking
            const baseHeight = 0.15
            const height = active ? 0.2 + Math.random() * 0.8 : baseHeight
            const color = isUserSpeaking ? 'bg-neon-purple' : 'bg-neon-cyan'
            return (
              <div
                key={i}
                className={`w-1 rounded-full transition-all ${color}`}
                style={{
                  height: `${height * 100}%`,
                  animation: active ? `wave-bounce ${0.6 + (i % 5) * 0.15}s ease-in-out infinite` : 'none',
                  animationDelay: `${i * 0.04}s`,
                  opacity: active ? 1 : 0.3,
                }}
              />
            )
          })}
        </div>
      </div>

      {/* Subtitle area */}
      <div className="relative z-10 px-6 mb-4 h-28 overflow-hidden">
        {currentSubtitle ? (
          <div className={`glass-card rounded-2xl p-4 animate-fade-in ${currentSubtitle.speaker === 'agent' ? 'border-l-2 border-l-neon-cyan' : 'border-l-2 border-l-neon-purple'}`}>
            <p className="text-xs text-muted-foreground mb-1">
              {currentSubtitle.speaker === 'agent' ? 'AI 助手' : session.userName}
            </p>
            <p className="text-sm leading-relaxed">
              {currentSubtitle.text}
              <span className="inline-block w-0.5 h-4 bg-neon-cyan ml-0.5 animate-glow-pulse" />
            </p>
          </div>
        ) : (
          <div className="h-full flex items-center justify-center">
            <p className="text-xs text-muted-foreground/50">等待对话...</p>
          </div>
        )}
      </div>

      {/* Transcript log */}
      {transcript.length > 0 && (
        <div className="relative z-10 px-6 mb-4 max-h-28 overflow-y-auto scrollbar-hidden">
          <div className="space-y-1.5">
            {transcript.slice(-5).map((entry) => (
              <div key={entry.id} className="text-xs leading-relaxed">
                <span className={entry.speaker === 'agent' ? 'text-neon-cyan font-medium' : 'text-neon-purple font-medium'}>
                  {entry.speaker === 'agent' ? 'AI' : '我'}:
                </span>
                <span className="text-muted-foreground ml-1.5">{entry.text}</span>
              </div>
            ))}
            <div ref={transcriptEndRef} />
          </div>
        </div>
      )}

      {/* Control buttons */}
      <div className="relative z-10 px-6 pb-10 pt-2">
        <div className="flex items-center justify-center gap-4">
          {/* Mute */}
          <button
            onClick={() => setMuted(!muted)}
            className={`w-14 h-14 rounded-full flex items-center justify-center transition-all ${muted ? 'bg-destructive/20 border border-destructive/30' : 'glass-card hover:bg-card/80'}`}
          >
            {muted ? <MicOff className="w-5 h-5 text-destructive" /> : <Mic className="w-5 h-5 text-foreground" />}
          </button>

          {/* End call */}
          <button
            onClick={handleEndCall}
            className="w-18 h-18 rounded-full bg-destructive flex items-center justify-center shadow-lg shadow-destructive/30 hover:scale-105 active:scale-95 transition-all"
            style={{ width: '4.5rem', height: '4.5rem' }}
          >
            <PhoneOff className="w-6 h-6 text-white" />
          </button>

          {/* Speaker */}
          <button
            onClick={() => setSpeakerOn(!speakerOn)}
            className={`w-14 h-14 rounded-full flex items-center justify-center transition-all ${!speakerOn ? 'bg-muted/40 border border-border' : 'glass-card hover:bg-card/80'}`}
          >
            {speakerOn ? <Volume2 className="w-5 h-5 text-foreground" /> : <VolumeX className="w-5 h-5 text-muted-foreground" />}
          </button>
        </div>
      </div>
    </div>
  )
}
