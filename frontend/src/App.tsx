// ponytail: 临时基座预览,用于确认 T1 设计令牌/字体/动效生效;组件阶段(T4+)会被真实页面替换。
const experts = [1, 2, 3, 4, 5, 6, 7, 8]

export default function App() {
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 24 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        <span
          className="live-badge"
          style={{
            color: 'var(--live-red)',
            textShadow: '0 0 8px var(--live-glow)',
            animation: 'live-blink 1s steps(2) infinite',
            fontSize: 16,
          }}
        >
          🔴 LIVE
        </span>
        <h1 style={{ fontSize: 20, margin: 0, color: 'var(--text-primary)' }}>AI 圆桌像素演播厅</h1>
      </div>

      <p style={{ color: 'var(--text-secondary)', fontSize: 12, margin: 0 }}>
        设计令牌基座就绪 · Fusion Pixel 12 · 深色演播厅底
      </p>

      {/* 专家 8 色板 + 主持人中性灰,确认色令牌 */}
      <div style={{ display: 'flex', gap: 6 }}>
        {experts.map((i) => (
          <div key={i} style={{ width: 28, height: 28, background: `var(--expert-${i})`, border: '1px solid var(--border-strong)' }} />
        ))}
        <div style={{ width: 28, height: 28, background: 'var(--host-color)', border: '1px solid var(--border-strong)' }} title="主持人" />
      </div>

      {/* 共识/分歧语义色 */}
      <div style={{ display: 'flex', gap: 12, fontSize: 12 }}>
        <span style={{ color: 'var(--consensus)' }}>■ 共识</span>
        <span style={{ color: 'var(--divergence)' }}>■ 分歧</span>
      </div>
    </div>
  )
}
