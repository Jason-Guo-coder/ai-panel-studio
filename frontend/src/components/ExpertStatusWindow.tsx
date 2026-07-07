import type { CSSProperties } from 'react'
import type { Participant, ExpertStatus } from '../types/dto'
import './ExpertStatusWindow.css'

interface Props {
  participant: Participant
  status: ExpertStatus // '待机' | '准备发言' | '发言中'
  focus: string | null
  isCurrentSpeaker: boolean
}

// status → 稳定 className 片段(避免中文类名),同时保留中文文本标签
const STATUS_CLASS: Record<ExpertStatus, string> = {
  待机: 'idle',
  准备发言: 'prepare',
  发言中: 'speaking',
}

export default function ExpertStatusWindow({
  participant,
  status,
  focus,
  isCurrentSpeaker,
}: Props) {
  const statusClass = STATUS_CLASS[status]
  const showFocus = status !== '待机' && !!focus

  // 专属色注入为 CSS 变量,供边框/聚光灯读取(§1「色 = 人」)
  const styleVars = {
    '--speaker-color': participant.color,
  } as CSSProperties

  return (
    <div
      className={`expert-window status-${statusClass}${
        isCurrentSpeaker ? ' is-current' : ''
      }`}
      style={styleVars}
      aria-label={`${participant.name} ${status}`}
    >
      {/* 像素头像 / 指示器:发言中显示可动的像素嘴 */}
      <div className="expert-avatar" aria-hidden="true">
        <span className="expert-eye expert-eye-l" />
        <span className="expert-eye expert-eye-r" />
        <span className="expert-mouth" />
      </div>

      <div className="expert-body">
        <div className="expert-headline">
          <span className="expert-name">{participant.name}</span>
          <span className="expert-status-label">{status}</span>
        </div>

        {participant.title && (
          <span className="expert-title">{participant.title}</span>
        )}

        {/* 发言中:像素声波 3–5 根,交错 animation-delay */}
        {status === '发言中' && (
          <div className="expert-wave" aria-hidden="true">
            <span className="wave-bar" />
            <span className="wave-bar" />
            <span className="wave-bar" />
            <span className="wave-bar" />
            <span className="wave-bar" />
          </div>
        )}

        {/* 关注点:仅准备/发言中显示 */}
        {showFocus && (
          <p className="expert-focus" title={focus ?? undefined}>
            {focus}
          </p>
        )}
      </div>
    </div>
  )
}
