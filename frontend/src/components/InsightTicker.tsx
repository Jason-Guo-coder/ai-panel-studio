import type { Insight } from '../types/dto'
import './InsightTicker.css'

interface Props {
  items: Insight[]
}

// 按 createdAt 升序 → 反转:最新条目排在最前(顶部),配合 slide-in 从顶部滑入(§3.5/§4)。
function sortNewestFirst(list: Insight[]): Insight[] {
  return [...list]
    .sort((a, b) => a.createdAt.localeCompare(b.createdAt))
    .reverse()
}

interface LaneProps {
  variant: 'consensus' | 'divergence'
  label: string
  glyph: string
  items: Insight[]
}

function Lane({ variant, label, glyph, items }: LaneProps) {
  return (
    <div className={`ticker-lane lane-${variant}`}>
      <div className="lane-header">
        <span className="lane-glyph" aria-hidden="true">
          {glyph}
        </span>
        <span className="lane-label">{label}</span>
      </div>

      <ul className="lane-rows">
        {items.length === 0 ? (
          <li className="lane-empty">暂无…</li>
        ) : (
          items.map((it) => (
            <li key={it.id} className="lane-row">
              <span className="row-glyph" aria-hidden="true">
                {glyph}
              </span>
              <span className="row-content">{it.content}</span>
            </li>
          ))
        )}
      </ul>
    </div>
  )
}

export default function InsightTicker({ items }: Props) {
  const consensus = sortNewestFirst(items.filter((i) => i.type === 'consensus'))
  const divergence = sortNewestFirst(
    items.filter((i) => i.type === 'divergence'),
  )

  return (
    <section className="insight-ticker" aria-label="共识与分歧记分牌">
      <Lane
        variant="consensus"
        label="共识"
        glyph="✓"
        items={consensus}
      />
      <Lane
        variant="divergence"
        label="分歧"
        glyph="⚡"
        items={divergence}
      />
    </section>
  )
}
