// 真实 REST 客户端(经 vite 代理 /api → 后端)。签名与页面契约不变。
import type { DiscussionSummary, DiscussionDetail, RosterResponse } from '../types/dto'

const BASE = '/api/discussions'

async function req<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(url, {
    headers: { 'Content-Type': 'application/json' },
    ...init,
  })
  if (!res.ok) {
    let msg = `请求失败 (${res.status})`
    try {
      const body = await res.json()
      if (body?.message) msg = body.message
    } catch {
      /* 无 JSON 错误体 */
    }
    throw new Error(msg)
  }
  if (res.status === 204) return undefined as T
  return res.json() as Promise<T>
}

export function getDiscussions(): Promise<DiscussionSummary[]> {
  return req(BASE)
}

export function getDiscussion(id: number): Promise<DiscussionDetail> {
  return req(`${BASE}/${id}`)
}

export function generateRoster(topic: string, expertCount = 4): Promise<RosterResponse> {
  return req(BASE, { method: 'POST', body: JSON.stringify({ topic, expertCount }) })
}

export function regenerate(id: number): Promise<RosterResponse> {
  return req(`${BASE}/${id}/regenerate`, { method: 'POST' })
}

export function confirm(id: number): Promise<{ id: number; status: string }> {
  return req(`${BASE}/${id}/confirm`, { method: 'POST' })
}

export function deleteDiscussion(id: number): Promise<void> {
  return req(`${BASE}/${id}`, { method: 'DELETE' })
}
