// Mock client —— 接口签名对齐 docs/API.md,当前全部读 mock。
// 后续替换真实后端:仅改本文件内实现(fetch / EventSource),页面不动。

import type {
  DiscussionSummary, DiscussionDetail, RosterResponse, Participant,
} from '../types/dto'
import { mockDiscussions } from './mockData'

// 运行时 store:以 mock 为初值;新建讨论追加于此,故首页/详情可见。
const store: DiscussionDetail[] = mockDiscussions.map((d) => ({ ...d }))

const delay = (ms = 220) => new Promise((r) => setTimeout(r, ms))

const EXPERT_PALETTE = ['#2563EB', '#DB2777', '#16A34A', '#F59E0B', '#7C3AED', '#0891B2', '#EA580C', '#0D9488']
const HOST_COLOR = '#6B7280'

// 生成阵容用的小型人设池(mock)
const HOST_NAMES = ['林知远', '周衡', '许明', '田甜', '罗盘', '沈渡']
const EXPERT_POOL = [
  { name: '陈默', profession: '软件架构师', title: '首席架构师' },
  { name: '苏晴', profession: '社会学者', title: '副教授' },
  { name: '王砚', profession: '创业者', title: 'CEO' },
  { name: '赵磊', profession: '一线从业者', title: '高级工程师' },
  { name: '何蓉', profession: '行为学者', title: '教授' },
  { name: '李维', profession: '行业分析师', title: '首席分析师' },
]

function nextId() {
  return Math.max(0, ...store.map((d) => d.discussion.id)) + 1
}

function buildRoster(discussionId: number, count: number): Participant[] {
  const host: Participant = {
    id: discussionId * 100,
    role: 'host',
    name: HOST_NAMES[discussionId % HOST_NAMES.length],
    profession: '圆桌主持人',
    title: '圆桌主持',
    stance: '中立引导',
    color: HOST_COLOR,
  }
  const experts: Participant[] = Array.from({ length: count }, (_, i) => {
    const p = EXPERT_POOL[i % EXPERT_POOL.length]
    return {
      id: discussionId * 100 + i + 1,
      role: 'expert',
      name: p.name,
      profession: p.profession,
      title: p.title,
      stance: `围绕议题的第 ${i + 1} 视角`,
      color: EXPERT_PALETTE[i % EXPERT_PALETTE.length], // expertIndex % 8
    }
  })
  return [host, ...experts]
}

export async function getDiscussions(): Promise<DiscussionSummary[]> {
  await delay()
  return store.map((d) => ({
    id: d.discussion.id,
    topic: d.discussion.topic,
    status: d.discussion.status,
    expertCount: d.discussion.expertCount,
    createdAt: d.discussion.createdAt,
  }))
}

export async function getDiscussion(id: number): Promise<DiscussionDetail> {
  await delay()
  const found = store.find((d) => d.discussion.id === id)
  if (!found) throw new Error('NOT_FOUND')
  return found
}

export async function generateRoster(topic: string, expertCount = 4): Promise<RosterResponse> {
  await delay(500) // 模拟 P1 生成耗时,便于展示加载态
  const id = nextId()
  const participants = buildRoster(id, expertCount)
  store.push({
    discussion: { id, topic, status: 'generating', expertCount, createdAt: new Date().toISOString().slice(0, 19).replace('T', ' '), summary: null },
    participants,
    speeches: [],
    insights: [],
  })
  return { id, topic, status: 'generating', expertCount, participants }
}

export async function regenerate(id: number): Promise<RosterResponse> {
  await delay(500)
  const found = store.find((d) => d.discussion.id === id)
  if (!found) throw new Error('NOT_FOUND')
  found.participants = buildRoster(id, found.discussion.expertCount)
  return { id, topic: found.discussion.topic, status: found.discussion.status, expertCount: found.discussion.expertCount, participants: found.participants }
}

export async function confirm(id: number): Promise<{ id: number; status: 'running' }> {
  await delay()
  const found = store.find((d) => d.discussion.id === id)
  if (!found) throw new Error('NOT_FOUND')
  found.discussion.status = 'running'
  return { id, status: 'running' }
}
