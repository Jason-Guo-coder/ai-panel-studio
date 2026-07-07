<div align="center">

# 🎙️ AI Panel Studio

### AI 圆桌讨论演播厅 · 全像素风实时演播厅

**输入一个话题,召集一支「虚拟智库」,观看一场由 AI 驱动、实时推进的圆桌辩论。**

<br/>

![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-5-646CFF?logo=vite&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-6DB33F?logo=springboot&logoColor=white)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5-red)
![SQLite](https://img.shields.io/badge/SQLite-WAL-003B57?logo=sqlite&logoColor=white)
![Deepseek](https://img.shields.io/badge/Deepseek-V4%20Pro-4D6BFE)
![Realtime](https://img.shields.io/badge/realtime-SSE-orange)
![Tests](https://img.shields.io/badge/tests-43%20backend%20%2B%205%20E2E-brightgreen)

</div>

---

**AI Panel Studio** 是一款本地运行的「AI 圆桌讨论」Web 应用。用户输入任意话题与专家人数,大模型动态生成一位主持人与若干专家嘉宾,进入**全像素风演播厅**——主持人负责开场 / 追问 / 串联 / 收尾,专家之间基于当前发言内容**非机械地**举手 / 抢答 / 补充 / 反驳,讨论过程中持续提炼**共识与分歧**并实时更新,最后由主持人以自然语言收尾。

> 🛠️ 全程以 **Claude Code + `deepseek-v4-pro` + Superpowers** 分阶段工程化开发:**引导 → SDD(契约)→ DDD(设计)→ TDD(测试)→ E2E(闭环)**,每阶段设人工闸门。开发过程与决策见 [`docs/WORKFLOW.md`](docs/WORKFLOW.md) 与 [`docs/PROMPT_LOG.md`](docs/PROMPT_LOG.md)。

---

## ✨ 核心亮点

- 🎬 **沉浸式像素演播厅** —— 全像素风 UI + 主席台 / 专家小窗 / 现场 Transcript / 实时共识分歧四区布局,当前发言人聚光灯 + 像素声波,像在看一场直播。
- ⚡ **真实时,不是轮询** —— 后端 `SseEmitter` 单向推流,前端原生 `EventSource` 订阅;新连接先收 `snapshot` 再接实时,断点续看 + 心跳保活 + 死连接清理。
- 🗣️ **非机械发言调度** —— "谁下一个说"由 LLM 基于内容相关性判定,Java 硬规则做护栏(反驳须有效 target、不许连说、主持人节奏、硬上限收尾),而非固定轮流。
- 🧩 **多讨论并行隔离** —— `ConcurrentHashMap<discussionId, DiscussionSession>` + 每表 `discussion_id`,不同讨论的状态 / 事件流 / transcript / 共识分歧互不串味。
- 🔒 **预算与安全护栏** —— 并发上限 + 每讨论发言硬上限护住 API 预算(实测 ≈¥0.05/场);API Key 仅后端从环境变量读、绝不入库/入日志;推理模型只取 `message.content`,隐藏 CoT 不外泄。
- 🧪 **全链路测试闭环** —— 单元 + 集成(并行隔离 / snapshot 先行 / 死连接清理)+ Playwright E2E;E2E 跑在确定性「假 AI」profile 上,**零花费、可重复、不 flaky**。

---

## 🎬 界面预览

> 📸 将演播厅截图放入 `docs/assets/` 后,取消下面注释即可在首页展示:
>
> <!-- ![演播厅](docs/assets/studio.png) -->
> <!-- ![嘉宾生成](docs/assets/roster.png) -->

---

## 🚀 快速开始

**前置环境:** JDK 17+ · Node 18+ · Maven 3.9+ ·(真实 AI 需要)Deepseek 账户

```bash
# 1) 克隆
git clone <repo> && cd ai-disscussion

# 2) 配置后端环境变量(Key 只在后端,绝不入库)
cp backend/.env.example backend/.env
#   编辑 backend/.env:填入 DEEPSEEK_API_KEY;DEEPSEEK_MODEL 默认 deepseek-v4-pro

# 3) 启动后端(自动建库 db/panel.db:schema 幂等建表 + seed 幂等灌 5 场样例)
cd backend
set -a; . ./.env; set +a          # 加载环境变量(zsh / bash)
mvn spring-boot:run               # → http://localhost:8080

# 4) 启动前端(另开终端;/api 自动代理到 :8080)
cd frontend
npm install
npm run dev                       # → http://localhost:5173
```

打开 **http://localhost:5173** —— 首页即有 **5 场样例讨论**,可直接观看或「发起新讨论」。

> 💡 **无 Key 也能跑**(确定性假 AI,零花费):后端以 `SPRING_PROFILES_ACTIVE=fake-ai` 启动,阵容 / 发言 / 总结由内置 `FakeAiService` 确定性产出。
> 🖱️ **一键启动**:双击项目根目录 `start.command`,自动起停前后端并在退出时清理。

---

## ⚙️ 环境变量

| 变量 | 说明 | 默认 |
|---|---|---|
| `DEEPSEEK_API_KEY` | Deepseek API Key(**仅后端从环境变量读,不入库 / 不入日志**) | 空 |
| `DEEPSEEK_MODEL` | 模型 id | `deepseek-v4-pro` |
| `DEEPSEEK_BASE_URL` | 接口地址 | `https://api.deepseek.com` |
| `DB_PATH` | SQLite 文件路径(相对后端工作目录) | `../db/panel.db` |
| `PANEL_MAX_CONCURRENT_DISCUSSIONS` | 并发活跃讨论上限(护预算) | `3` |
| `PANEL_MAX_SPEECHES` | 每讨论发言硬上限(护预算) | `16` |

---

## 🧱 技术选型

| 层 | 选型 | 理由 |
|---|---|---|
| 前端 | React + Vite + TypeScript | 组件化契合四区演播厅;TS 对齐 API 契约 |
| 实时 | **SSE(原生 `EventSource`)** | 数据流单向(引擎 → 前端),WebSocket 双向能力浪费;原生自动重连贴合"加入 / 续看" |
| 后端 | Spring Boot 3 + MyBatis-Plus | `SseEmitter` 内置零额外依赖;MyBatis-Plus 省样板 |
| 存储 | SQLite(WAL) | 作业指定;WAL 让多引擎线程写 + 请求线程读不互斥 |
| AI | Deepseek V4 Pro | 推理模型;客户端只取 `message.content`、不读 `reasoning_content`(CoT 不泄漏) |
| 测试 | JUnit5 + Mockito + MockMvc + Playwright | 单元 / 集成 / 端到端;E2E 用 fake-ai profile,零花费、确定性 |

> **架构立场:** 后端是唯一引擎,自驱多讨论并行推进、写库、SSE 广播;前端是纯观察者,不驱动任何讨论逻辑。

---

## 🔌 主要 API

### REST

| 方法 | 路径 | 说明 |
|---|---|---|
| `GET` | `/api/discussions` | 讨论列表 |
| `POST` | `/api/discussions` | 创建 + P1 生成阵容(`{topic, expertCount}`)→ `generating` |
| `POST` | `/api/discussions/{id}/regenerate` | 重新生成阵容(仅 `generating`) |
| `POST` | `/api/discussions/{id}/confirm` | 确认 → `running` + 提交引擎循环 |
| `GET` | `/api/discussions/{id}` | 详情 / 历史(discussion + participants + speeches + insights) |
| `GET` | `/api/discussions/{id}/stream` | **SSE** 实时流 |

统一错误体 `{code, message}`:`VALIDATION_ERROR`(400)· `NOT_FOUND`(404)· `INVALID_STATE`(409)· `AI_UPSTREAM_ERROR`(502)。

### SSE 事件(7 种,命名事件)

| event | 何时 | payload 要点 |
|---|---|---|
| `snapshot` | 新连接接入(先于实时) | 当前发言人 + 每专家 status / focus |
| `speech` | 有新发言 | speech 行(含 `seq`) |
| `insight` | 提炼出共识 / 分歧 | insight 行(含 `createdAt`) |
| `status` | 专家状态变化 | `{participantId, status, focus}` |
| `summary` | 收尾 | 自然语言总结 |
| `finished` | 结束 | `{discussionId}` |
| `error` | AI 调用失败 | `{message}` |

> 完整契约见 [`docs/API.md`](docs/API.md);数据模型见 [`docs/architecture.md`](docs/architecture.md) / [`db/schema.sql`](db/schema.sql)。

---

## ✅ 已完成能力

- **首页**:讨论列表(`running` / `finished` / `interrupted`)+ 三态视觉(加载 / 空台 / 雪花屏)
- **嘉宾生成**:P1 动态生成 1 主持 + N 专家(姓名 / 职业 / Title / 立场 / **专属色后端按调色板指派**),确认前可重生成
- **演播厅**:非机械发言调度、专家状态小窗三态、实时共识 / 分歧、现场 transcript(不显内部事件)、主持人自然语言总结
- **多讨论并行隔离**:状态 / 事件流 / transcript / 共识分歧互不串味
- **SSE 实时**:snapshot 先行 + 断点续看(先拉历史再续订)+ 心跳保活 + 死连接清理
- **失败降级**:重试 1 次 → 强制主持人回合 → 连崩推 `error` + 暂停
- **全像素风 UI** + 三档响应式(超宽 / 桌面 / 窄屏,各区独立滚动)+ 运动安全(`prefers-reduced-motion`)
- **测试**:后端 43(单元 + 集成 A2 / A7 / A8 / A9)+ Playwright E2E 5(fake-ai)

## 🧭 后续改进方向

- **崩溃恢复**:重启从 SQLite 重建上下文续跑 `running` 讨论(当前标 `interrupted`)
- **智能提前收尾**:主持人判定议题充分即收尾(当前仅硬上限)
- **流式输出**:P2 逐 token 流式呈现,进一步增强实时感
- **全员实时思考**:为空闲专家也生成公开关注点(当前仅当前 / 下一位)
- 多用户 / 鉴权、限流与配额、可观测(调用追踪 / 成本看板)

---

## 📁 项目结构

```
ai-disscussion/
├─ docs/       PRD · architecture(ER)· API · design · BRAINSTORM · PROMPT_LOG · WORKFLOW
├─ db/         schema.sql(建表 + 索引 + WAL)· seed.sql(≥5 样例讨论,幂等)
├─ backend/    Spring Boot(controller / service / engine / sse / ai / mapper / entity)+ 测试
└─ frontend/   React + Vite + TS(pages / components / hooks / api / types / styles)+ e2e(Playwright)
```

## 📚 开发文档

| 文档 | 内容 |
|---|---|
| [PRD.md](docs/PRD.md) | 产品需求:定位 / 痛点 / MVP 范围 / 页面 / 核心流程 / 风险 |
| [architecture.md](docs/architecture.md) | 技术架构:数据模型(ER)/ AI 编排 / 红线清单 / 验收标准 |
| [API.md](docs/API.md) | 接口契约:REST + 7 种 SSE 事件 |
| [design.md](docs/design.md) | 全像素风演播厅视觉 / 交互基线 |
| [PROMPT_LOG.md](docs/PROMPT_LOG.md) | 核心 Prompt 记录(五段,四范式) |
| [WORKFLOW.md](docs/WORKFLOW.md) | 开发过程思路 & 工作流 + 真实冒烟花费 + 实施计划 |
| [BRAINSTORM.md](docs/BRAINSTORM.md) | 需求澄清收敛结论 |

<div align="center">
<br/>

**AI 可以承担大量重复性编码,但程序员必须始终握住方向盘、刹车与规则。**

</div>
