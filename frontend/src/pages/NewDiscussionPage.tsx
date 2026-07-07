import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import PixelAvatarCard from '../components/PixelAvatarCard'
import PixelState from '../components/PixelState'
import { generateRoster, regenerate, confirm } from '../api/client'
import type { RosterResponse } from '../types/dto'
import './NewDiscussionPage.css'

type Phase = 'form' | 'generating' | 'roster' | 'error'

export default function NewDiscussionPage() {
  const navigate = useNavigate()
  const [topic, setTopic] = useState('')
  const [count, setCount] = useState(4)
  const [phase, setPhase] = useState<Phase>('form')
  const [roster, setRoster] = useState<RosterResponse | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const canGenerate = topic.trim().length > 0

  const doGenerate = () => {
    if (!canGenerate) return
    setPhase('generating')
    generateRoster(topic.trim(), count)
      .then((r) => { setRoster(r); setPhase('roster') })
      .catch(() => setPhase('error'))
  }

  const doRegenerate = () => {
    if (!roster) return
    setPhase('generating')
    regenerate(roster.id)
      .then((r) => { setRoster(r); setPhase('roster') })
      .catch(() => setPhase('error'))
  }

  const doConfirm = () => {
    if (!roster) return
    setSubmitting(true)
    confirm(roster.id)
      .then(() => navigate(`/discussions/${roster.id}`))
      .catch(() => { setSubmitting(false); setPhase('error') })
  }

  const host = roster?.participants.find((p) => p.role === 'host')
  const experts = roster?.participants.filter((p) => p.role === 'expert') ?? []

  return (
    <div className="newd">
      <header className="newd__header">
        <button type="button" className="newd__back" onClick={() => navigate('/')}>← 返回</button>
        <h1 className="newd__title">发起圆桌讨论</h1>
      </header>

      <main className="newd__body scroll-area">
        <section className="newd__form">
          <label className="newd__field">
            <span className="newd__label">讨论话题</span>
            <input
              className="newd__input"
              value={topic}
              onChange={(e) => setTopic(e.target.value)}
              placeholder="输入任意议题,例如:AI 会取代初级程序员吗?"
              maxLength={200}
            />
          </label>
          <label className="newd__field newd__field--count">
            <span className="newd__label">专家人数</span>
            <select className="newd__input" value={count} onChange={(e) => setCount(Number(e.target.value))}>
              {[2, 3, 4, 5, 6].map((n) => <option key={n} value={n}>{n} 位</option>)}
            </select>
          </label>
          <button type="button" className="newd__gen" disabled={!canGenerate || phase === 'generating'} onClick={doGenerate}>
            {phase === 'roster' ? '重新生成阵容' : '生成阵容'}
          </button>
        </section>

        <section className="newd__result">
          {phase === 'generating' && <PixelState kind="loading" message="正在召集虚拟智库…" />}
          {phase === 'error' && <PixelState kind="error" message="阵容生成失败" onRetry={roster ? doRegenerate : doGenerate} />}
          {phase === 'roster' && roster && (
            <>
              <div className="newd__rostrum">
                {host && <PixelAvatarCard participant={host} />}
              </div>
              <div className="newd__experts">
                {experts.map((e) => <PixelAvatarCard key={e.id} participant={e} />)}
              </div>
              <div className="newd__actions">
                <button type="button" className="newd__secondary" onClick={doRegenerate}>重新生成</button>
                <button type="button" className="newd__confirm" disabled={submitting} onClick={doConfirm}>
                  {submitting ? '进入中…' : '确认阵容,进入演播厅 →'}
                </button>
              </div>
            </>
          )}
        </section>
      </main>
    </div>
  )
}
