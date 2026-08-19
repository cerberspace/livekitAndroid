import { useState, useCallback } from 'react'
import ConnectScreen from './screens/ConnectScreen'
import CallScreen from './screens/CallScreen'
import HistoryScreen from './screens/HistoryScreen'

export type Screen = 'connect' | 'call' | 'history'

export interface CallSession {
  roomName: string
  userName: string
  startTime: number
  duration: number
  transcript: TranscriptEntry[]
}

export interface TranscriptEntry {
  id: string
  speaker: 'user' | 'agent'
  text: string
  timestamp: number
}

// Mock history data for prototype
const mockHistory: CallSession[] = [
  {
    roomName: 'voice-room-001',
    userName: '我',
    startTime: Date.now() - 86400000,
    duration: 184,
    transcript: [
      { id: '1', speaker: 'agent', text: '你好！我是你的 AI 语音助手，有什么可以帮你的吗？', timestamp: Date.now() - 86400000 },
      { id: '2', speaker: 'user', text: '帮我总结一下今天的日程安排', timestamp: Date.now() - 86400000 + 5000 },
      { id: '3', speaker: 'agent', text: '好的，你今天有三个会议，分别是上午10点的产品评审、下午2点的设计同步和下午4点的1on1。', timestamp: Date.now() - 86400000 + 10000 },
    ],
  },
  {
    roomName: 'voice-room-002',
    userName: '我',
    startTime: Date.now() - 172800000,
    duration: 92,
    transcript: [
      { id: '1', speaker: 'agent', text: '欢迎回来！需要继续上次的对话吗？', timestamp: Date.now() - 172800000 },
      { id: '2', speaker: 'user', text: '不用了，给我推荐一首放松的音乐', timestamp: Date.now() - 172800000 + 5000 },
      { id: '3', speaker: 'agent', text: '推荐你听听 Ludovico Einaudi 的 Nuvole Bianche，非常适合放松心情。', timestamp: Date.now() - 172800000 + 12000 },
    ],
  },
  {
    roomName: 'voice-room-003',
    userName: '我',
    startTime: Date.now() - 259200000,
    duration: 356,
    transcript: [
      { id: '1', speaker: 'agent', text: '你好！今天感觉怎么样？', timestamp: Date.now() - 259200000 },
      { id: '2', speaker: 'user', text: '有点累，想聊聊天', timestamp: Date.now() - 259200000 + 3000 },
      { id: '3', speaker: 'agent', text: '当然可以！我随时都在。你想聊什么话题？工作、生活还是兴趣爱好？', timestamp: Date.now() - 259200000 + 8000 },
    ],
  },
]

function App() {
  const [screen, setScreen] = useState<Screen>('connect')
  const [currentSession, setCurrentSession] = useState<CallSession | null>(null)
  const [history, setHistory] = useState<CallSession[]>(mockHistory)

  const handleConnect = useCallback((roomName: string, userName: string) => {
    const session: CallSession = {
      roomName,
      userName,
      startTime: Date.now(),
      duration: 0,
      transcript: [],
    }
    setCurrentSession(session)
    setScreen('call')
  }, [])

  const handleEndCall = useCallback((transcript: TranscriptEntry[], duration: number) => {
    if (currentSession) {
      const completed: CallSession = {
        ...currentSession,
        duration,
        transcript,
      }
      setHistory(prev => [completed, ...prev])
    }
    setCurrentSession(null)
    setScreen('connect')
  }, [])

  return (
    <div className="min-h-screen bg-background flex items-center justify-center p-4 overflow-hidden relative">
      {/* Ambient background glow */}
      <div className="fixed inset-0 pointer-events-none">
        <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-neon-cyan/10 rounded-full blur-3xl animate-glow-pulse" />
        <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-neon-purple/10 rounded-full blur-3xl animate-glow-pulse" style={{ animationDelay: '1s' }} />
      </div>

      {/* Phone Frame */}
      <div className="phone-frame relative z-10">
        {screen === 'connect' && (
          <ConnectScreen onConnect={handleConnect} onHistory={() => setScreen('history')} />
        )}
        {screen === 'call' && currentSession && (
          <CallScreen
            session={currentSession}
            onEndCall={handleEndCall}
          />
        )}
        {screen === 'history' && (
          <HistoryScreen
            history={history}
            onBack={() => setScreen('connect')}
            onClearHistory={() => setHistory([])}
          />
        )}
      </div>
    </div>
  )
}

export default App
