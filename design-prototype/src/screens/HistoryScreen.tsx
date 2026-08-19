import { useState } from 'react'
import { ChevronLeft, Clock, Trash2, MessageCircle, Phone, ChevronRight } from 'lucide-react'
import { Button } from '@/components/ui/button'
import type { CallSession } from '@/App'

interface HistoryScreenProps {
  history: CallSession[]
  onBack: () => void
  onClearHistory: () => void
}

function formatRelativeTime(timestamp: number): string {
  const diff = Date.now() - timestamp
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  if (hours < 24) return `${hours} 小时前`
  if (days < 7) return `${days} 天前`
  return new Date(timestamp).toLocaleDateString('zh-CN')
}

function formatDuration(seconds: number): string {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}分${s.toString().padStart(2, '0')}秒`
}

export default function HistoryScreen({ history, onBack, onClearHistory }: HistoryScreenProps) {
  const [selectedCall, setSelectedCall] = useState<CallSession | null>(null)
  const [showClearConfirm, setShowClearConfirm] = useState(false)

  if (selectedCall) {
    return (
      <div className="h-full flex flex-col relative overflow-hidden">
        {/* Header */}
        <div className="relative z-10 px-6 pt-16 pb-4 flex items-center gap-3">
          <button
            onClick={() => setSelectedCall(null)}
            className="w-10 h-10 rounded-xl glass-card flex items-center justify-center hover:bg-card/80 transition-colors"
          >
            <ChevronLeft className="w-5 h-5 text-foreground" />
          </button>
          <div>
            <h2 className="text-lg font-bold">通话详情</h2>
            <p className="text-xs text-muted-foreground">{new Date(selectedCall.startTime).toLocaleString('zh-CN')}</p>
          </div>
        </div>

        {/* Call info */}
        <div className="relative z-10 px-6 mb-4">
          <div className="glass-card rounded-2xl p-4 flex items-center gap-4">
            <div className="w-12 h-12 rounded-full bg-neon-gradient-soft border border-neon-cyan/20 flex items-center justify-center">
              <Phone className="w-5 h-5 text-neon-cyan" />
            </div>
            <div className="flex-1">
              <p className="text-sm font-semibold">{selectedCall.roomName}</p>
              <div className="flex items-center gap-3 mt-1">
                <span className="flex items-center gap-1 text-xs text-muted-foreground">
                  <Clock className="w-3 h-3" />
                  {formatDuration(selectedCall.duration)}
                </span>
                <span className="flex items-center gap-1 text-xs text-muted-foreground">
                  <MessageCircle className="w-3 h-3" />
                  {selectedCall.transcript.length} 条消息
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Transcript */}
        <div className="relative z-10 flex-1 px-6 pb-8 overflow-y-auto scrollbar-hidden">
          <p className="text-xs text-muted-foreground uppercase tracking-wider mb-3 font-semibold">对话记录</p>
          <div className="space-y-3">
            {selectedCall.transcript.map((entry) => (
              <div
                key={entry.id}
                className={`flex ${entry.speaker === 'user' ? 'justify-end' : 'justify-start'}`}
              >
                <div
                  className={`max-w-[80%] rounded-2xl p-3 ${
                    entry.speaker === 'agent'
                      ? 'glass-card border-l-2 border-l-neon-cyan'
                      : 'bg-neon-purple/10 border border-neon-purple/20 border-r-2'
                  }`}
                >
                  <p className="text-xs text-muted-foreground mb-1">
                    {entry.speaker === 'agent' ? 'AI 助手' : selectedCall.userName}
                  </p>
                  <p className="text-sm leading-relaxed">{entry.text}</p>
                  <p className="text-xs text-muted-foreground/50 mt-1">
                    {new Date(entry.timestamp).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="h-full flex flex-col relative overflow-hidden">
      {/* Header */}
      <div className="relative z-10 px-6 pt-16 pb-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            onClick={onBack}
            className="w-10 h-10 rounded-xl glass-card flex items-center justify-center hover:bg-card/80 transition-colors"
          >
            <ChevronLeft className="w-5 h-5 text-foreground" />
          </button>
          <div>
            <h1 className="text-xl font-bold">通话历史</h1>
            <p className="text-xs text-muted-foreground">{history.length} 条记录</p>
          </div>
        </div>
        {history.length > 0 && (
          <button
            onClick={() => setShowClearConfirm(!showClearConfirm)}
            className="w-10 h-10 rounded-xl glass-card flex items-center justify-center hover:bg-destructive/10 transition-colors"
          >
            <Trash2 className="w-4 h-4 text-muted-foreground" />
          </button>
        )}
      </div>

      {/* Clear confirm */}
      {showClearConfirm && (
        <div className="relative z-10 px-6 mb-4 animate-slide-up">
          <div className="glass-card rounded-2xl p-4 border border-destructive/20">
            <p className="text-sm text-foreground mb-3">确定要清空所有通话记录吗？</p>
            <div className="flex gap-2">
              <Button variant="glass" size="sm" className="flex-1" onClick={() => setShowClearConfirm(false)}>
                取消
              </Button>
              <Button variant="destructive" size="sm" className="flex-1" onClick={() => { onClearHistory(); setShowClearConfirm(false) }}>
                清空
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* History list */}
      <div className="relative z-10 flex-1 px-6 pb-8 overflow-y-auto scrollbar-hidden">
        {history.length === 0 ? (
          <div className="h-full flex flex-col items-center justify-center text-center">
            <div className="w-20 h-20 rounded-full glass-card flex items-center justify-center mb-4">
              <Phone className="w-8 h-8 text-muted-foreground/30" />
            </div>
            <p className="text-sm text-muted-foreground mb-1">暂无通话记录</p>
            <p className="text-xs text-muted-foreground/50">完成一次通话后，记录会显示在这里</p>
          </div>
        ) : (
          <div className="space-y-3">
            {history.map((call, index) => (
              <button
                key={index}
                onClick={() => setSelectedCall(call)}
                className="w-full text-left glass-card rounded-2xl p-4 hover:bg-card/80 transition-all group"
              >
                <div className="flex items-start gap-3">
                  {/* Icon */}
                  <div className="w-11 h-11 rounded-xl bg-neon-gradient-soft border border-neon-cyan/20 flex items-center justify-center flex-shrink-0">
                    <Phone className="w-4 h-4 text-neon-cyan" />
                  </div>

                  {/* Content */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between mb-1">
                      <p className="text-sm font-semibold truncate">{call.roomName}</p>
                      <ChevronRight className="w-4 h-4 text-muted-foreground/30 group-hover:text-muted-foreground transition-colors flex-shrink-0" />
                    </div>
                    {/* Transcript preview */}
                    {call.transcript.length > 0 && (
                      <p className="text-xs text-muted-foreground truncate mb-2">
                        <span className={call.transcript[0].speaker === 'agent' ? 'text-neon-cyan/70' : 'text-neon-purple/70'}>
                          {call.transcript[0].speaker === 'agent' ? 'AI' : '我'}:
                        </span>
                        {' '}{call.transcript[0].text}
                      </p>
                    )}
                    {/* Meta */}
                    <div className="flex items-center gap-3 text-xs text-muted-foreground/60">
                      <span className="flex items-center gap-1">
                        <Clock className="w-3 h-3" />
                        {formatDuration(call.duration)}
                      </span>
                      <span>·</span>
                      <span>{formatRelativeTime(call.startTime)}</span>
                      <span>·</span>
                      <span className="flex items-center gap-1">
                        <MessageCircle className="w-3 h-3" />
                        {call.transcript.length}
                      </span>
                    </div>
                  </div>
                </div>
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
