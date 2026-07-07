// 模拟 SSE 实时流:先回放讨论历史(snapshot),再逐条推进 speech/status/insight,末尾 summary/finished。
// 签名与未来 useDiscussionStream(EventSource)对齐,替换真实流只改实现。

import { useEffect, useState } from 'react'
import type { Participant, Speech, Insight, ExpertStatus } from '../types/dto'
import { getDiscussion } from './../api/client'

export interface ExpertLiveState {
  status: ExpertStatus
  focus: string | null
}

export type StreamPhase = 'connecting' | 'live' | 'finished' | 'empty' | 'interrupted' | 'error'

export interface MockStream {
  phase: StreamPhase
  topic: string
  participants: Participant[]
  speeches: Speech[]
  insights: Insight[]
  expertStates: Record<number, ExpertLiveState>
  currentSpeakerId: number | null
  summary: string | null
}

const EMPTY: MockStream = {
  phase: 'connecting', topic: '', participants: [], speeches: [], insights: [],
  expertStates: {}, currentSpeakerId: null, summary: null,
}

export function useMockStream(id: number | null, stepMs = 1600): MockStream {
  const [state, setState] = useState<MockStream>(EMPTY)

  useEffect(() => {
    if (id == null) return
    let cancelled = false
    let timer: number | undefined
    setState(EMPTY)

    getDiscussion(id).then((detail) => {
      if (cancelled) return
      const { participants, discussion } = detail
      const experts = participants.filter((p) => p.role === 'expert')
      const baseStates: Record<number, ExpertLiveState> = {}
      experts.forEach((e) => { baseStates[e.id] = { status: '待机', focus: null } })

      const speeches = [...detail.speeches].sort((a, b) => a.seq - b.seq)
      const insights = [...detail.insights].sort((a, b) => (a.createdAt < b.createdAt ? -1 : 1))
      const isFinishedStatus = discussion.status === 'finished'

      // 已中断:不回放,直接静态展示全部历史 + interrupted 相位(架构:历史可看、无实时)
      if (discussion.status === 'interrupted') {
        setState({
          phase: 'interrupted', topic: discussion.topic, participants,
          speeches, insights, expertStates: baseStates, currentSpeakerId: null, summary: null,
        })
        return
      }

      // 无发言(新建讨论 roster-only)→ 空台等待开场
      if (speeches.length === 0) {
        setState({
          phase: discussion.status === 'finished' ? 'finished' : 'empty',
          topic: discussion.topic, participants, speeches: [], insights: [],
          expertStates: baseStates, currentSpeakerId: null, summary: discussion.summary,
        })
        return
      }

      // snapshot:接入即小窗非空白(全待机)
      const states: Record<number, ExpertLiveState> = { ...baseStates }
      const shownSpeeches: Speech[] = []
      const shownInsights: Insight[] = []
      let i = 0
      setState({
        phase: 'live', topic: discussion.topic, participants,
        speeches: [], insights: [], expertStates: { ...states }, currentSpeakerId: null, summary: null,
      })

      const tick = () => {
        if (cancelled) return
        const s = speeches[i]
        // 复位所有专家 → 待机
        Object.keys(states).forEach((k) => { states[+k] = { status: '待机', focus: null } })
        const speaker = participants.find((p) => p.id === s.participantId)
        if (speaker?.role === 'expert') {
          states[speaker.id] = { status: '发言中', focus: speaker.stance }
        }
        shownSpeeches.push(s)
        // 按时间序 flush 到当前发言时刻的共识/分歧(多出现在主持人回合附近)
        while (shownInsights.length < insights.length && insights[shownInsights.length].createdAt <= s.createdAt) {
          shownInsights.push(insights[shownInsights.length])
        }
        i++
        // 预告下一位专家 → 准备发言
        const next = speeches[i]
        if (next) {
          const np = participants.find((p) => p.id === next.participantId)
          if (np?.role === 'expert' && np.id !== speaker?.id) {
            states[np.id] = { status: '准备发言', focus: np.stance }
          }
        }
        const done = i >= speeches.length
        // running:回放追平后保持 live(不出 summary);finished:收尾出 summary。
        setState({
          phase: done ? (isFinishedStatus ? 'finished' : 'live') : 'live',
          topic: discussion.topic, participants,
          expertStates: { ...states },
          speeches: [...shownSpeeches],
          insights: [...shownInsights],
          currentSpeakerId: s.participantId,
          summary: done && isFinishedStatus ? discussion.summary : null,
        })
        if (done && timer) clearInterval(timer)
      }

      timer = window.setInterval(tick, stepMs)
    }).catch(() => {
      if (!cancelled) setState({ ...EMPTY, phase: 'error' })
    })

    return () => { cancelled = true; if (timer) clearInterval(timer) }
  }, [id, stepMs])

  return state
}
