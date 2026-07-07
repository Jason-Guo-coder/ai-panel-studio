// REST DTO —— 对齐 docs/API.md §1。字段 camelCase。

export type DiscussionStatus = 'generating' | 'running' | 'finished' | 'interrupted'
export type ParticipantRole = 'host' | 'expert'
export type ExpertStatus = '待机' | '准备发言' | '发言中'
export type InsightType = 'consensus' | 'divergence'
export type ReactionType =
  | '开场' | '串联' | '追问' | '收尾'
  | '举手' | '抢答' | '补充' | '反驳'

export interface DiscussionSummary {
  id: number
  topic: string
  status: DiscussionStatus
  expertCount: number
  createdAt: string
}

export interface Participant {
  id: number
  role: ParticipantRole
  name: string
  profession: string
  title: string
  stance: string
  color: string
}

export interface Speech {
  id: number
  participantId: number
  content: string
  reactionType: ReactionType
  targetSpeechId?: number | null
  seq: number
  createdAt: string
}

export interface Insight {
  id: number
  type: InsightType
  content: string
  createdAt: string
}

// GET /api/discussions/{id}
export interface DiscussionDetail {
  discussion: DiscussionSummary & { summary: string | null }
  participants: Participant[]
  speeches: Speech[]
  insights: Insight[]
}

// POST /api/discussions  与 /regenerate 的返回
export interface RosterResponse {
  id: number
  topic: string
  status: DiscussionStatus
  expertCount: number
  participants: Participant[]
}
