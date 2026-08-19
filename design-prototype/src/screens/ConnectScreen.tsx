import { useState } from 'react'
import { Phone, History, Settings, Sparkles } from 'lucide-react'
import { Button } from '@/components/ui/button'

interface ConnectScreenProps {
  onConnect: (roomName: string, userName: string) => void
  onHistory: () => void
}

export default function ConnectScreen({ onConnect, onHistory }: ConnectScreenProps) {
  const [roomName, setRoomName] = useState('')
  const [userName, setUserName] = useState('')
  const [showSettings, setShowSettings] = useState(false)

  const handleConnect = () => {
    const room = roomName.trim() || `room-${Math.random().toString(36).slice(2, 8)}`
    const name = userName.trim() || `用户${Math.floor(Math.random() * 1000)}`
    onConnect(room, name)
  }

  return (
    <div className="h-full flex flex-col relative overflow-hidden">
      {/* Background image overlay */}
      <div
        className="absolute inset-0 opacity-30"
        style={{ backgroundImage: 'url(/images/neon-bg.png)', backgroundSize: 'cover', backgroundPosition: 'center' }}
      />

      {/* Content */}
      <div className="relative z-10 flex flex-col h-full px-6 pt-16 pb-8">
        {/* Top bar */}
        <div className="flex justify-between items-center mb-8">
          <button
            onClick={() => setShowSettings(!showSettings)}
            className="w-10 h-10 rounded-xl glass-card flex items-center justify-center hover:bg-card/80 transition-colors"
          >
            <Settings className="w-5 h-5 text-muted-foreground" />
          </button>
          <span className="text-xs text-muted-foreground font-mono">v1.0.0</span>
        </div>

        {/* Logo */}
        <div className="flex-1 flex flex-col items-center justify-center -mt-8">
          {/* Animated logo */}
          <div className="relative w-28 h-28 mb-6">
            <div className="absolute inset-0 rounded-full bg-neon-gradient blur-2xl opacity-40 animate-glow-pulse" />
            <div className="relative w-28 h-28 rounded-full glass-card flex items-center justify-center neon-border">
              <div className="flex items-end gap-1 h-12">
                {[0.3, 0.6, 1, 0.7, 0.4, 0.8, 0.5].map((h, i) => (
                  <div
                    key={i}
                    className="w-1.5 rounded-full bg-neon-gradient"
                    style={{
                      height: `${h * 100}%`,
                      animation: `wave-bounce 0.8s ease-in-out infinite`,
                      animationDelay: `${i * 0.1}s`,
                    }}
                  />
                ))}
              </div>
            </div>
          </div>

          {/* Title */}
          <h1 className="text-4xl font-extrabold tracking-tight mb-2">
            <span className="bg-neon-gradient bg-clip-text text-transparent">VoxAI</span>
          </h1>
          <p className="text-sm text-muted-foreground mb-2">实时 AI 语音助手</p>
          <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-neon-cyan/10 border border-neon-cyan/20">
            <div className="w-1.5 h-1.5 rounded-full bg-neon-cyan animate-glow-pulse" />
            <span className="text-xs text-neon-cyan font-medium">Powered by LiveKit</span>
          </div>
        </div>

        {/* Input fields */}
        <div className="space-y-3 mb-6">
          <div className="relative">
            <input
              type="text"
              value={roomName}
              onChange={(e) => setRoomName(e.target.value)}
              placeholder="房间名称（可选）"
              className="w-full h-14 px-5 rounded-2xl glass-card text-sm text-foreground placeholder:text-muted-foreground/50 focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all"
            />
          </div>
          <div className="relative">
            <input
              type="text"
              value={userName}
              onChange={(e) => setUserName(e.target.value)}
              placeholder="你的昵称（可选）"
              onKeyDown={(e) => e.key === 'Enter' && handleConnect()}
              className="w-full h-14 px-5 rounded-2xl glass-card text-sm text-foreground placeholder:text-muted-foreground/50 focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all"
            />
          </div>
        </div>

        {/* Action buttons */}
        <div className="space-y-3">
          <Button
            variant="neon"
            size="xl"
            className="w-full"
            onClick={handleConnect}
          >
            <Phone className="w-5 h-5 mr-2" />
            开始语音通话
          </Button>
          <Button
            variant="glass"
            size="lg"
            className="w-full"
            onClick={onHistory}
          >
            <History className="w-4 h-4 mr-2 text-muted-foreground" />
            通话历史
          </Button>
        </div>

        {/* Feature hints */}
        <div className="flex justify-center gap-4 mt-6">
          {[
            { icon: Sparkles, label: 'AI 对话' },
            { icon: Phone, label: '实时通话' },
            { icon: History, label: '历史记录' },
          ].map(({ icon: Icon, label }) => (
            <div key={label} className="flex items-center gap-1.5 text-xs text-muted-foreground">
              <Icon className="w-3.5 h-3.5 text-neon-cyan/70" />
              <span>{label}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Settings panel */}
      {showSettings && (
        <div className="absolute bottom-0 left-0 right-0 z-20 p-6 pb-10 glass-card rounded-t-3xl border-t border-white/10 animate-slide-up">
          <h3 className="text-sm font-semibold mb-4 text-foreground">连接设置</h3>
          <div className="space-y-3">
            <div>
              <label className="text-xs text-muted-foreground">LiveKit 服务器 URL</label>
              <input
                type="text"
                defaultValue="wss://your-livekit-server"
                className="w-full h-10 px-3 mt-1 rounded-lg bg-secondary/50 text-xs text-foreground border border-border focus:outline-none focus:ring-1 focus:ring-primary/50"
              />
            </div>
            <div>
              <label className="text-xs text-muted-foreground">API Token</label>
              <input
                type="password"
                defaultValue=""
                placeholder="输入 Token"
                className="w-full h-10 px-3 mt-1 rounded-lg bg-secondary/50 text-xs text-foreground border border-border focus:outline-none focus:ring-1 focus:ring-primary/50"
              />
            </div>
          </div>
          <Button variant="default" size="sm" className="w-full mt-4" onClick={() => setShowSettings(false)}>
            保存
          </Button>
        </div>
      )}
    </div>
  )
}
