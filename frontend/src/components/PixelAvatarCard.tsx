import type { CSSProperties } from 'react'
import type { Participant } from '../types/dto'
import './PixelAvatarCard.css'

interface Props {
  participant: Participant
  compact?: boolean // 主持人横向紧凑变体(演播厅主席台用)
  speaking?: boolean // 主持人正在发言(高亮,颜色同主持人中性灰)
}

// 由 participant.id 派生稳定 hash,保证同一人特征恒定、不同人相异。
function hashId(id: number): number {
  let h = 2166136261 ^ id
  h = Math.imul(h ^ (id >>> 8), 16777619)
  h = Math.imul(h ^ (id >>> 16), 16777619)
  return (h >>> 0)
}

// 发色候选(深底友好,偏中性),按 hash 取,避免与专属色撞。
const HAIR_SHADES = ['#2b1c10', '#3a2a1a', '#4a3524', '#1f1f24', '#5a4632', '#6b4a2a']
// 肤色候选。
const SKIN_TONES = ['#f0c8a0', '#e8b98c', '#d9a06b', '#c68642', '#b5764f']
const HAIR_STYLES = ['', 'style-b', 'style-c'] as const

interface Features {
  hair: string
  skin: string
  hairStyle: string
  glasses: boolean
}

function deriveFeatures(id: number): Features {
  const h = hashId(id)
  return {
    hair: HAIR_SHADES[h % HAIR_SHADES.length],
    skin: SKIN_TONES[(h >>> 3) % SKIN_TONES.length],
    hairStyle: HAIR_STYLES[(h >>> 6) % HAIR_STYLES.length],
    glasses: ((h >>> 9) & 1) === 1,
  }
}

// 像素头像(CSS 绘制,零 PNG)。特征经 style 变量注入。
function PixelHead({ features }: { features: Features }) {
  const canvasStyle = {
    '--skin': features.skin,
    '--hair': features.hair,
  } as CSSProperties
  return (
    <div className="avatar-canvas" style={canvasStyle} aria-hidden="true">
      <div className="pixel-suit" />
      <div className="pixel-lapel-l" />
      <div className="pixel-lapel-r" />
      <div className="pixel-collar" />
      <div className="pixel-tie" />
      <div className="pixel-head" />
      <div className={`pixel-hair ${features.hairStyle}`} />
      <div className="pixel-eye left" />
      <div className="pixel-eye right" />
      {features.glasses && <div className="pixel-glasses" />}
      <div className="pixel-mouth" />
    </div>
  )
}

export default function PixelAvatarCard({ participant, compact = false, speaking = false }: Props) {
  const { role, name, profession, title, stance, color } = participant
  const features = deriveFeatures(participant.id)

  if (role === 'host') {
    if (compact) {
      // 演播厅主席台:横向紧凑条,仍中性灰 + “主持”徽标 + 主席台描边,明显区别于专家。
      return (
        <div className={`avatar-card is-host compact${speaking ? ' is-speaking' : ''}`} aria-label={`主持人 ${name}`}>
          <div className="host-avatar-wrap">
            <div className="host-crown" aria-hidden="true" />
            <PixelHead features={features} />
          </div>
          <div className="avatar-info">
            <div className="avatar-headline">
              <span className="host-badge">主持</span>
              <span className="avatar-name" title={name}>{name}</span>
              {speaking && <span className="host-speaking">● 发言中</span>}
            </div>
            <span className="avatar-meta">{profession}·{title}</span>
          </div>
        </div>
      )
    }
    // 完整变体(嘉宾确认页):中性灰 + 主席台皇冠 + “主持”徽标,竖排大卡。
    return (
      <div className="avatar-card is-host" aria-label={`主持人 ${name}`}>
        <div className="host-crown" aria-hidden="true" />
        <PixelHead features={features} />
        <span className="host-badge">主持</span>
        <span className="avatar-name" title={name}>
          {name}
        </span>
        <span className="avatar-meta">
          {profession}·{title}
        </span>
        <span className="avatar-stance">{stance}</span>
      </div>
    )
  }

  // 专家变体:头像外框 / 底部名条 = participant.color。
  const frameStyle = { '--frame': color } as CSSProperties
  return (
    <div
      className="avatar-card is-expert"
      style={frameStyle}
      aria-label={`嘉宾 ${name}`}
    >
      <PixelHead features={features} />
      <span className="avatar-name" title={name}>
        {name}
      </span>
      <span className="avatar-meta">
        {profession}·{title}
      </span>
      <span className="avatar-stance">{stance}</span>
      <div className="avatar-colorbar" />
    </div>
  )
}
