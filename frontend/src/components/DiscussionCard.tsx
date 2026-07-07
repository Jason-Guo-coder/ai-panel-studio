import type { KeyboardEvent } from 'react'
import type { DiscussionStatus } from '../types/dto'
import './DiscussionCard.css'

interface Props {
  topic: string
  status: DiscussionStatus
  expertCount: number
  createdAt: string
  onEnter: () => void
}

const STATUS_LABEL: Record<Exclude<DiscussionStatus, 'running'>, string> = {
  generating: '生成中…',
  finished: '✓ 已结束',
  interrupted: '信号中断',
}

export default function DiscussionCard({
  topic,
  status,
  expertCount,
  createdAt,
  onEnter,
}: Props) {
  const handleKeyDown = (e: KeyboardEvent<HTMLDivElement>) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault()
      onEnter()
    }
  }

  return (
    <div
      className={`discussion-card status-${status}`}
      role="button"
      tabIndex={0}
      onClick={onEnter}
      onKeyDown={handleKeyDown}
      aria-label={`进入讨论:${topic}`}
    >
      {status === 'running' && (
        <span className="live-badge" aria-label="正在直播">
          <span className="live-dot" />
          LIVE
        </span>
      )}

      <p className="discussion-topic" title={topic}>
        {topic}
      </p>

      <div className="discussion-meta">
        <span className="discussion-count">{expertCount} 位嘉宾</span>
        <span className="discussion-time">{createdAt}</span>
      </div>

      {status !== 'running' && (
        <span className={`status-tag status-tag-${status}`}>
          {STATUS_LABEL[status]}
        </span>
      )}
    </div>
  )
}
