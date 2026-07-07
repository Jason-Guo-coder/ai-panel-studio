// 真实实时流:先 GET /{id} 拉历史(断点续看),再订阅 /stream(EventSource,原生自动重连)。
// 按 speech.seq / insight.createdAt 去重排序,消除"历史↔实时"竞态。
import { useEffect, useState } from 'react'
import type { Participant, Speech, Insight, ExpertStatus } from '../types/dto'
import { getDiscussion } from '../api/client'

export interface ExpertLiveState {
  status: ExpertStatus
  focus: string | null
}

export type StreamPhase = 'connecting' | 'live' | 'finished' | 'empty' | 'interrupted' | 'error'

export interface DiscussionStream {
  phase: StreamPhase
  topic: string
  participants: Participant[]
  speeches: Speech[]
  insights: Insight[]
  expertStates: Record<number, ExpertLiveState>
  currentSpeakerId: number | null
  summary: string | null
}

const EMPTY: DiscussionStream = {
  phase: 'connecting', topic: '', participants: [], speeches: [], insights: [],
  expertStates: {}, currentSpeakerId: null, summary: null,
}

const bySeq = (a: Speech, b: Speech) => a.seq - b.seq
const byCreated = (a: Insight, b: Insight) => (a.createdAt < b.createdAt ? -1 : 1)

// 讨论结束时把所有专家复位为待机(避免最后一位卡在发言态)
function resetIdle(states: Record<number, ExpertLiveState>): Record<number, ExpertLiveState> {
  const next: Record<number, ExpertLiveState> = {}
  for (const k of Object.keys(states)) next[Number(k)] = { status: '待机', focus: null }
  return next
}

export function useDiscussionStream(id: number | null): DiscussionStream {
  const [state, setState] = useState<DiscussionStream>(EMPTY)

  useEffect(() => {
    if (id == null) return
    let cancelled = false
    let es: EventSource | null = null
    const seenSpeech = new Set<number>()
    const seenInsight = new Set<number>()
    setState(EMPTY)

    // 1) 断点续看:先拉历史
    getDiscussion(id).then((detail) => {
      if (cancelled) return
      const expertStates: Record<number, ExpertLiveState> = {}
      detail.participants.filter((p) => p.role === 'expert')
        .forEach((e) => { expertStates[e.id] = { status: '待机', focus: null } })

      const speeches = [...detail.speeches].sort(bySeq)
      const insights = [...detail.insights].sort(byCreated)
      speeches.forEach((s) => seenSpeech.add(s.id))
      insights.forEach((i) => seenInsight.add(i.id))

      const status = detail.discussion.status
      const phase: StreamPhase =
        status === 'finished' ? 'finished'
          : status === 'interrupted' ? 'interrupted'
            : speeches.length === 0 ? 'empty' : 'live'

      const over = status === 'finished' || status === 'interrupted'
      setState({
        phase, topic: detail.discussion.topic, participants: detail.participants,
        speeches, insights, expertStates,
        // 已结束/已中断:无人发言;进行中:延续最后发言人
        currentSpeakerId: over ? null : (speeches.length ? speeches[speeches.length - 1].participantId : null),
        summary: detail.discussion.summary,
      })

      // 已结束/已中断:历史即全部,无需订阅实时
      if (status === 'finished' || status === 'interrupted') return

      // 2) 订阅实时
      es = new EventSource(`/api/discussions/${id}/stream`)

      es.addEventListener('snapshot', (e) => {
        const d = JSON.parse((e as MessageEvent).data)
        setState((prev) => {
          const next = { ...prev.expertStates }
          for (const ex of d.experts ?? []) next[ex.participantId] = { status: ex.status, focus: ex.focus }
          return { ...prev, currentSpeakerId: d.currentSpeakerId ?? prev.currentSpeakerId, expertStates: next }
        })
      })

      es.addEventListener('speech', (e) => {
        const s: Speech = JSON.parse((e as MessageEvent).data)
        if (seenSpeech.has(s.id)) return
        seenSpeech.add(s.id)
        setState((prev) => ({
          ...prev,
          phase: prev.phase === 'empty' ? 'live' : prev.phase,
          currentSpeakerId: s.participantId,
          speeches: [...prev.speeches, s].sort(bySeq),
        }))
      })

      es.addEventListener('insight', (e) => {
        const i: Insight = JSON.parse((e as MessageEvent).data)
        if (seenInsight.has(i.id)) return
        seenInsight.add(i.id)
        setState((prev) => ({ ...prev, insights: [...prev.insights, i].sort(byCreated) }))
      })

      es.addEventListener('status', (e) => {
        const d = JSON.parse((e as MessageEvent).data)
        setState((prev) => ({
          ...prev,
          expertStates: { ...prev.expertStates, [d.participantId]: { status: d.status, focus: d.focus } },
        }))
      })

      es.addEventListener('summary', (e) => {
        const d = JSON.parse((e as MessageEvent).data)
        setState((prev) => ({ ...prev, summary: d.summary }))
      })

      es.addEventListener('finished', () => {
        // 结束:复位发言态,最后一位不再卡在"发言中"
        setState((prev) => ({
          ...prev,
          phase: 'finished',
          currentSpeakerId: null,
          expertStates: resetIdle(prev.expertStates),
        }))
        es?.close()
      })

      // 后端 AI 失败:命名 error 事件带 data(与原生连接 error 区分)
      es.addEventListener('error', (e) => {
        const me = e as MessageEvent
        if (me.data) {
          setState((prev) => ({ ...prev, phase: 'error' }))
          es?.close()
        }
      })

      // 原生连接错误:EventSource 自动重连;彻底关闭才亮错误态
      es.onerror = () => {
        if (es && es.readyState === EventSource.CLOSED) {
          setState((prev) => (prev.phase === 'finished' ? prev : { ...prev, phase: 'error' }))
        }
      }
    }).catch(() => {
      if (!cancelled) setState((prev) => ({ ...prev, phase: 'error' }))
    })

    return () => { cancelled = true; es?.close() }
  }, [id])

  return state
}
