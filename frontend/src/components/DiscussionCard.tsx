import type { KeyboardEvent, MouseEvent } from 'react'
import type { DiscussionStatus } from '../types/dto'
import './DiscussionCard.css'

interface Props {
  topic: string
  status: DiscussionStatus
  expertCount: number
  createdAt: string
  onEnter: () => void
  onDelete?: () => void // 提供则显示删除按钮(running 除外)
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
  onDelete,
}: Props) {
  const handleKeyDown = (e: KeyboardEvent<HTMLDivElement>) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault()
      onEnter()
    }
  }
  const handleDelete = (e: MouseEvent) => {
    e.stopPropagation()
    onDelete?.()
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

      {onDelete && status !== 'running' && (
        <button
          type="button"
          className="discussion-del"
          onClick={handleDelete}
          aria-label="删除讨论"
          title="删除讨论"
        >
          ✕
        </button>
      )}
    </div>
  )
}
