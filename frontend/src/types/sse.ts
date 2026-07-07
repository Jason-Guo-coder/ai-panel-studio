// SSE 事件 payload —— 对齐 docs/API.md §2 的 7 种事件。

import type { Speech, Insight, ExpertStatus } from './dto'

export interface ExpertSnapshot {
  participantId: number
  status: ExpertStatus
  focus: string | null
}

export interface SnapshotPayload {
  currentSpeakerId: number | null
  experts: ExpertSnapshot[]
}

export interface StatusPayload {
  participantId: number
  status: ExpertStatus
  focus: string | null
}

export interface SummaryPayload {
  summary: string
}

export interface FinishedPayload {
  discussionId: number
}

export interface ErrorPayload {
  message: string
}

// 判别联合:event 名 = SSE 命名事件
export type StreamEvent =
  | { event: 'snapshot'; data: SnapshotPayload }
  | { event: 'speech'; data: Speech }
  | { event: 'insight'; data: Insight }
  | { event: 'status'; data: StatusPayload }
  | { event: 'summary'; data: SummaryPayload }
  | { event: 'finished'; data: FinishedPayload }
  | { event: 'error'; data: ErrorPayload }

export type StreamEventName = StreamEvent['event']
