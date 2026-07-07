import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import DiscussionCard from '../components/DiscussionCard'
import PixelState from '../components/PixelState'
import { getDiscussions } from '../api/client'
import type { DiscussionSummary } from '../types/dto'
import './HomePage.css'

export default function HomePage() {
  const navigate = useNavigate()
  const [items, setItems] = useState<DiscussionSummary[]>([])
  const [phase, setPhase] = useState<'loading' | 'ready' | 'error'>('loading')

  const load = useCallback(() => {
    setPhase('loading')
    getDiscussions()
      .then((list) => {
        // 列表只显示 running/finished/interrupted;generating 是私有中间态,不上首页。
        setItems(list.filter((d) => d.status !== 'generating'))
        setPhase('ready')
      })
      .catch(() => setPhase('error'))
  }, [])

  useEffect(() => { load() }, [load])

  return (
    <div className="home">
      <header className="home__header">
        <div className="home__brand">
          <span className="home__logo">▚</span>
          <h1 className="home__title">AI 圆桌像素演播厅</h1>
        </div>
        <button type="button" className="home__new" onClick={() => navigate('/new')}>
          + 发起新讨论
        </button>
      </header>

      <main className="home__body scroll-area">
        {phase === 'loading' && <PixelState kind="loading" message="信号接入中…" />}
        {phase === 'error' && <PixelState kind="error" message="讨论列表加载失败" onRetry={load} />}
        {phase === 'ready' && items.length === 0 && (
          <PixelState kind="empty" message="暂无进行中的圆桌,发起一场?" />
        )}
        {phase === 'ready' && items.length > 0 && (
          <div className="home__grid">
            {items.map((d) => (
              <DiscussionCard
                key={d.id}
                topic={d.topic}
                status={d.status}
                expertCount={d.expertCount}
                createdAt={d.createdAt}
                onEnter={() => navigate(`/discussions/${d.id}`)}
              />
            ))}
          </div>
        )}
      </main>
    </div>
  )
}
