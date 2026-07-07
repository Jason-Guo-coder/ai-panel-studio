// §5 三态占位:加载(信号接入中)/ 空(演播厅空台)/ 错误(信号中断雪花屏 + 重试)。
// 首页与演播厅复用,像素/CRT 隐喻,纯 CSS。
import './PixelState.css'

interface Props {
  kind: 'loading' | 'empty' | 'error'
  message: string
  onRetry?: () => void
}

export default function PixelState({ kind, message, onRetry }: Props) {
  return (
    <div className={`pixel-state pixel-state--${kind}`} role="status" aria-live="polite">
      {kind === 'loading' && (
        <div className="pixel-state__spinner" aria-hidden="true">
          <span /><span /><span /><span />
        </div>
      )}
      {kind === 'empty' && <div className="pixel-state__stage" aria-hidden="true" />}
      {kind === 'error' && <div className="pixel-state__snow" aria-hidden="true" />}

      <p className="pixel-state__msg">{message}</p>

      {kind === 'error' && onRetry && (
        <button type="button" className="pixel-state__retry" onClick={onRetry}>
          重试
        </button>
      )}
    </div>
  )
}
