import "./SummaryPanel.css";

interface Props {
  summary: string | null;
  loading: boolean;
}

// §3.6 主持人总结区:中性灰主持人 accent + summary 只渲染自然语言(红线 L10,禁 JSON)。
export default function SummaryPanel({ summary, loading }: Props) {
  return (
    <section className="summary-panel" aria-label="主持人总结">
      <h2 className="summary-panel__title">主持人总结</h2>

      <div className="summary-panel__body">
        {loading ? (
          <div className="summary-panel__loading" role="status" aria-live="polite">
            <span className="summary-panel__spinner" aria-hidden="true" />
            <span className="summary-panel__loading-text">生成中…</span>
          </div>
        ) : summary === null ? (
          <p className="summary-panel__empty">讨论结束后,主持人将在此给出总结</p>
        ) : (
          // L10:summary 是纯字符串,作为一段自然语言渲染,绝不上任何 JSON/调试输出。
          <p className="summary-panel__text">{summary}</p>
        )}
      </div>
    </section>
  );
}
