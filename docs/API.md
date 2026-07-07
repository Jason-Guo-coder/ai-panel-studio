# AI 圆桌讨论 MVP · 接口契约(API)

> SDD 阶段接口契约,前后端共同遵守。REST 用于 CRUD 与断点续看的历史加载;SSE 用于实时推流。
> Base URL(本地):`http://localhost:8080`,前缀 `/api`。所有时间为 `datetime('now')` 文本(`YYYY-MM-DD HH:MM:SS`)。
> **安全红线:** Deepseek API Key 仅后端环境变量读取,任何响应/SSE payload 都不含 Key。

---

## 1. REST 接口

### 1.1 讨论列表
`GET /api/discussions`

首页展示所有讨论。

**200 响应**
```json
[
  { "id": 1, "topic": "AI 是否会取代初级程序员?", "status": "running", "expertCount": 4, "createdAt": "2026-07-01 10:00:00" }
]
```
> `status`: `generating` | `running` | `finished` | `interrupted`

---

### 1.2 发起讨论(生成阵容)
`POST /api/discussions`

创建讨论并调用 **P1** 生成主持人 + 专家阵容。此时 `status=generating`,尚未开跑。

**请求**
```json
{ "topic": "远程办公是不是未来?", "expertCount": 4 }
```
校验:`topic` 非空(≤200 字);`expertCount` ∈ [2, 6],默认 4。

**201 响应**
```json
{
  "id": 6,
  "topic": "远程办公是不是未来?",
  "status": "generating",
  "expertCount": 4,
  "participants": [
    { "id": 26, "role": "host",   "name": "…", "profession": "…", "title": "圆桌主持", "stance": "中立引导", "color": "#6B7280" },
    { "id": 27, "role": "expert", "name": "…", "profession": "…", "title": "…", "stance": "…", "color": "#2563EB" }
  ]
}
```

**502**:P1 调用失败 → `{ "code": "AI_UPSTREAM_ERROR", "message": "阵容生成失败,请重试" }`

---

### 1.3 重新生成阵容
`POST /api/discussions/{id}/regenerate`

对 `generating` 状态的讨论重跑 P1,替换 participants。仅 `generating` 可调,否则 409。

**200 响应**:同 1.2 的 `participants` 字段。

---

### 1.4 确认阵容,开跑
`POST /api/discussions/{id}/confirm`

用户确认 → `status: generating→running`,提交引擎循环(后端自驱)。**这是单写者交接点。**

- 仅 `generating` 可确认,否则 409 `{ "code": "INVALID_STATE" }`。
- 若已达并发上限(3),排队,仍返回 202。

**202 响应**
```json
{ "id": 6, "status": "running" }
```

---

### 1.5 讨论详情 / 历史(断点续看)
`GET /api/discussions/{id}`

进入演播厅或加入观察时**先调此接口拉全量历史**,再订阅 SSE。

**200 响应**
```json
{
  "discussion": { "id": 1, "topic": "…", "status": "finished", "expertCount": 4, "summary": "…", "createdAt": "…" },
  "participants": [ { "id": 1, "role": "host", "name": "…", "color": "#6B7280", "profession": "…", "title": "…", "stance": "…" } ],
  "speeches": [
    { "id": 1, "participantId": 1, "content": "…", "reactionType": "开场", "seq": 1, "createdAt": "…" }
  ],
  "insights": [
    { "id": 1, "type": "consensus", "content": "…", "createdAt": "…" }
  ]
}
```
> `speeches` 按 `seq` 升序;`insights` 按 `createdAt` 升序。`reactionType`/`targetSpeechId` 仅用于前端调试,**transcript 不渲染内部事件类型**。`summary` 为 `null` 表示未结束。

**404**:讨论不存在。

---

## 2. SSE 事件流

`GET /api/discussions/{id}/stream`

返回 `text/event-stream`。前端用原生 `EventSource` 订阅。**连接建立后服务器先推一个 `snapshot`,再接实时事件。** 每 ~20s 发 `:ping` 注释心跳。

**订阅时序(前端):**
```
1) GET /api/discussions/{id}      → 渲染历史 transcript / insight / 小窗
2) new EventSource(.../stream)     → 收 snapshot 重建小窗当前态,再接实时
3) 按 speech.seq / insight.createdAt 去重排序,消除历史↔实时竞态
```

### 7 种事件

| event | 触发时机 | payload |
|---|---|---|
| `snapshot` | 新连接接入 | 当前小窗全量态 |
| `speech`   | 新发言入库后 | 一条 speech |
| `insight`  | 提炼出共识/分歧 | 一条 insight |
| `status`   | 专家状态变化 | 单个专家状态(focus 并入) |
| `summary`  | 讨论收尾 | 自然语言总结 |
| `finished` | 讨论结束 | 结束信号,前端停流 |
| `error`    | AI 调用失败 | 错误消息,前端亮错误态 |

### payload 示例

**snapshot**
```
event: snapshot
data: {"currentSpeakerId":3,"experts":[
  {"participantId":2,"status":"待机","focus":null},
  {"participantId":3,"status":"发言中","focus":"想反驳上一条关于自动化的判断"},
  {"participantId":4,"status":"准备发言","focus":"补充工具增强的视角"},
  {"participantId":5,"status":"待机","focus":null}]}
```
> `status`: `待机` | `准备发言` | `发言中`。仅被点名的下一位/当前发言人有真实 `focus`,其余 `null`。

**speech**
```
event: speech
data: {"id":8,"participantId":2,"content":"重复性代码会被自动化。","reactionType":"举手","targetSpeechId":null,"seq":2,"createdAt":"2026-07-01 10:00:20"}
```

**insight**(仅主持人回合产出)
```
event: insight
data: {"id":1,"type":"consensus","content":"重复性编码会被 AI 大幅自动化。","createdAt":"2026-07-01 10:01:12"}
```

**status**
```
event: status
data: {"participantId":4,"status":"准备发言","focus":"补充工具增强的视角"}
```

**summary**(禁止 JSON 原文上屏,前端仅渲染此自然语言)
```
event: summary
data: {"summary":"本场讨论收敛出的共识是……"}
```

**finished**
```
event: finished
data: {"discussionId":1}
```

**error**
```
event: error
data: {"message":"AI 调用连续失败,讨论已暂停,请稍后重试"}
```

---

## 3. 约定与错误

- **错误体统一**:`{ "code": "STRING_CODE", "message": "中文提示" }`。
- **常见 code**:`INVALID_STATE`(状态不允许该操作)、`NOT_FOUND`、`AI_UPSTREAM_ERROR`(模型调用失败)、`VALIDATION_ERROR`(入参非法)。
- **断线重连**:`EventSource` 自动重连;重连后走同一时序(重拉历史 + 重收 snapshot),**不依赖 Last-Event-ID**。
- **死连接清理**:服务端 `onCompletion/onTimeout/onError` 移除 emitter,不向死连接推送。

---

*(SDD 交付物完成:PRD / architecture / schema.sql / seed.sql / API。下一步进入 DDD 前端设计或 TDD 核心逻辑。)*
