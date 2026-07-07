import { useEffect, useRef } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import PixelAvatarCard from '../components/PixelAvatarCard'
import ExpertStatusWindow from '../components/ExpertStatusWindow'
import SpeechNameplate from '../components/SpeechNameplate'
import InsightTicker from '../components/InsightTicker'
import SummaryPanel from '../components/SummaryPanel'
import PixelState from '../components/PixelState'
import { useDiscussionStream, type StreamPhase } from '../hooks/useDiscussionStream'
import './StudioPage.css'

const PILL: Record<StreamPhase, { text: string; cls: string }> = {
  connecting: { text: '接入中', cls: 'pill-dim' },
  live: { text: '● LIVE', cls: 'pill-live' },
  finished: { text: '已结束', cls: 'pill-done' },
  interrupted: { text: '信号中断', cls: 'pill-warn' },
  empty: { text: '等待开场', cls: 'pill-dim' },
  error: { text: '错误', cls: 'pill-warn' },
}

export default function StudioPage() {
  const navigate = useNavigate()
  const { id } = useParams()
  const stream = useDiscussionStream(id ? Number(id) : null)

  const transcriptRef = useRef<HTMLDivElement>(null)
  // 新发言自动滚到底
  useEffect(() => {
    const el = transcriptRef.current
    if (el) el.scrollTop = el.scrollHeight
  }, [stream.speeches.length])

  // 全屏三态:接入中 / 错误
  if (stream.phase === 'connecting') {
    return <div className="studio studio--center"><PixelState kind="loading" message="📡 信号接入中…" /></div>
  }
  if (stream.phase === 'error') {
    return <div className="studio studio--center"><PixelState kind="error" message="🔌 信号中断,无法接入该讨论" onRetry={() => navigate(0)} /></div>
  }

  const host = stream.participants.find((p) => p.role === 'host')
  const experts = stream.participants.filter((p) => p.role === 'expert')
  const pill = PILL[stream.phase]

  return (
    <div className="studio">
      <header className="studio__header region">
        <button type="button" className="studio__back" onClick={() => navigate('/')}>← 首页</button>
        <span className={`studio__pill ${pill.cls}`}>{pill.text}</span>
        <h1 className="studio__topic" title={stream.topic}>{stream.topic}</h1>
      </header>

      {/* B 主席台 + 专家小窗 */}
      <section className="studio__stage region" aria-label="主席台与专家">
        <div className="studio__rostrum">
          {host && <PixelAvatarCard participant={host} compact />}
        </div>
        <div className="studio__experts scroll-area">
          {experts.map((e) => {
            const st = stream.expertStates[e.id]
            return (
              <ExpertStatusWindow
                key={e.id}
                participant={e}
                status={st?.status ?? '待机'}
                focus={st?.focus ?? null}
                isCurrentSpeaker={stream.currentSpeakerId === e.id}
              />
            )
          })}
        </div>
      </section>

      {/* A 现场 Transcript */}
      <section className="studio__transcript region" aria-label="现场记录">
        <div className="region__title">现场 Transcript</div>
        {stream.phase === 'interrupted' && (
          <div className="studio__banner">该讨论已中断,以下为已产生的记录</div>
        )}
        <div className="studio__speeches scroll-area" ref={transcriptRef}>
          {stream.speeches.length === 0 ? (
            <PixelState kind="empty" message="信号已接入,等待主持人开场…" />
          ) : (
            stream.speeches.map((sp) => {
              const p = stream.participants.find((x) => x.id === sp.participantId)
              return p ? <SpeechNameplate key={sp.id} participant={p} content={sp.content} /> : null
            })
          )}
        </div>
      </section>

      {/* C 共识 / 分歧 */}
      <section className="studio__insights region scroll-area" aria-label="共识与分歧">
        <div className="region__title">共识 / 分歧</div>
        <InsightTicker items={stream.insights} />
      </section>

      {/* D 主持人总结 */}
      <section className="studio__summary region scroll-area" aria-label="主持人总结">
        <SummaryPanel summary={stream.summary} loading={false} />
      </section>
    </div>
  )
}
