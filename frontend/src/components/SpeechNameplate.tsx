import type { Participant } from '../types/dto'
import './SpeechNameplate.css'

interface Props {
  participant: Participant
  content: string
}

export default function SpeechNameplate({ participant, content }: Props) {
  const isHost = participant.role === 'host'
  const blockColor = isHost ? 'var(--host-color)' : participant.color

  return (
    <div className={`speech-nameplate${isHost ? ' speech-nameplate--host' : ''}`}>
      <span className="speech-nameplate__block" style={{ background: blockColor }} aria-hidden="true" />
      <div className="speech-nameplate__body">
        <div className="speech-nameplate__meta">
          {isHost && <span className="speech-nameplate__tag">主持</span>}
          <span className="speech-nameplate__name">{participant.name}</span>
          <span className="speech-nameplate__title">·{participant.title}</span>
        </div>
        <p className="speech-nameplate__content">{content}</p>
      </div>
    </div>
  )
}
