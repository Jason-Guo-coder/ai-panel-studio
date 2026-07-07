import './ConfirmModal.css'

interface Props {
  message: string
  confirmText?: string
  cancelText?: string
  onConfirm: () => void
  onCancel: () => void
}

// 像素风确认弹窗:全屏遮罩 + 居中对话框,替代浏览器原生 confirm。
export default function ConfirmModal({
  message,
  confirmText = '删除',
  cancelText = '取消',
  onConfirm,
  onCancel,
}: Props) {
  return (
    <div className="confirm-overlay" role="presentation" onClick={onCancel}>
      <div
        className="confirm-dialog"
        role="alertdialog"
        aria-modal="true"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="confirm-title">确认操作</div>
        <p className="confirm-msg">{message}</p>
        <div className="confirm-actions">
          <button type="button" className="confirm-btn confirm-cancel" onClick={onCancel}>
            {cancelText}
          </button>
          <button type="button" className="confirm-btn confirm-ok" onClick={onConfirm} autoFocus>
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  )
}
