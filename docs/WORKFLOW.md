# 开发过程思路 & 工作流说明

## 一、开发流程:Claude Code + deepseek-v4-pro + Superpowers + ponytail + 闸门式人工审查

本项目全程以 **Claude Code** 为唯一开发环境,接入 **deepseek-v4-pro** 作为产品运行时的大模型,并用 **Superpowers** 技能族把开发切成清晰的阶段范式,每个阶段结束设**人工闸门**(我确认后才进下一阶段),避免 AI 一路跑偏。

**阶段范式(多范式融合):**
1. **引导(brainstorming)**:逐组澄清需求,用 `ponytail` 视角先砍过度设计——把作业里"画像愿望"(录制/多用户)与"硬功能"分离,锁定 MVP 边界与"后端自驱、前端纯观察"的总架构。
2. **SDD(契约/模型驱动)**:先出 `PRD / architecture / API / schema / seed`,把字段名、状态枚举、7 种 SSE 事件**钉死成契约**——让后续前后端实现只填空、不发明,从源头压制大模型幻觉。
3. **DDD(设计驱动)**:先定 `design.md`(全像素风演播厅视觉/交互基线),再用 `subagent-driven-development` 并行分派六组件,每个走"spec 合规审查→代码质量审查"双闸门;三页面 + 纯 mock 先把沉浸感/实时感跑通可视验收。
4. **TDD(测试驱动)**:核心逻辑(嘉宾生成/发言调度/共识提炼)严格 RED→GREEN→REFACTOR——先给测试清单确认覆盖,再让 33 条测试先全红、逐单元实现转绿,**靠实现转绿、绝不改断言凑绿**。
5. **E2E(质量闭环)**:建 Web 层(REST + SSE + 引擎真跑),集成测试补 A2/A7/A8/A9;前端切真实接口;Playwright 端到端跑在**确定性"假 AI"** profile 上(零 Deepseek 花费、可重复);真实 Deepseek 只做一次小成本冒烟。
6. **收尾**:`verification-before-completion` 实跑全套贴真实输出;`requesting/receiving-code-review` 派**独立子代理**对抗式审查,逐条核实采纳/驳回。

**Git 提交呈层级演进**:`docs/schema → docs(design) → ui-components → tests(RED) → feat: logic → feat(web/sse/engine) → test(web/e2e) → fix → docs`,过程可溯。

## 二、AI 协同中的 3 个典型问题及解决路径

**1. `speech.id=seq` 主键碰撞——单测的盲区,靠集成测试 + systematic-debugging 逮住。**
引擎为让内存 transcript 有 id 做 target 校验,把 `seq` 手设成了 speech 主键。**单元测试全绿**(mock 了 Mapper,不碰真实约束),但集成测试 A7(两讨论并行)一跑就炸 `SQLITE_CONSTRAINT_PRIMARYKEY`——不同讨论的 speech.id 必撞。解决:用 `systematic-debugging` 查异常栈定位根因(不盲改),改为不设 id、让 MyBatis-Plus AUTO 回填真实自增 id。**教训:mock 边界会掩盖数据库约束类 bug,集成测试不可省。**

**2. 集成测试的 SQLite 隔离——测试基建必须贴近生产语义。**
集成库初用 `:memory:?cache=shared`,结果 `journal_mode=memory`(非 WAL)读阻塞写,且上下文关闭时内存库被销毁,引擎线程写超时。换成 `target` 文件库 + WAL(读不阻塞写、不销毁)后稳定。**教训:测试环境与生产的存储语义差异会制造假失败。**

**3. `insight` 事件从未广播——独立代码评审逮住无测试覆盖的功能缺口。**
提交前派独立子代理做对抗式评审,发现 `InsightExtractor` 只入库、引擎从不调 `events.insight`——**没有任何测试覆盖这条广播链路**,导致直播观众看不到实时共识/分歧(只有刷新才从 REST 拿到)。逐条核实后采纳,修复并**补一条回归测试**锁死。同批还修了收尾破 16 上限、snapshot 排序竞态等。**教训:独立评审能发现测试写不到的盲区,采纳要逐条给理由。**

## 三、对"工程化 AI 开发"的理解

一键生成能出"完美黑箱",但工程化 AI 开发要的是**对 AI 输出的掌控力**——用契约、测试、审查把大模型的不确定性"夹"住:

- **契约先行压制幻觉**:SDD 阶段把字段/枚举/事件钉死,AI 实现时无从发明,幻觉没有生存空间。
- **测试是 AI 的缰绳**:TDD 先写会失败的测试再让 AI 实现,"看着它失败"才证明测试真在测目标;绝不为凑绿改断言。
- **确定性替身隔离花费与不确定性**:用 `@Profile("fake-ai")` 的 FakeAiService 让 E2E 零花费、可重复、不 flaky,把真实大模型的花费与波动压到一次冒烟。
- **人机分工**:AI 擅长生成与铺量,人负责判断、边界与取舍;`ponytail` 持续砍过度设计,`subagent` 并行提效,**闸门式人工确认**保证每阶段不跑偏。
- **过程 > 结果**:清晰、可溯、每步有据的"草稿成品",比不可解释的完美品更可信——因为它可维护、可复盘、可交接。

deepseek-v4-pro 是推理模型,客户端严格只取 `message.content`、不读 `reasoning_content`,既保护隐藏思维链不外泄,也守住"不展示真实 CoT"的产品红线——这类边界,正是工程化 AI 开发里人必须替 AI 把关的地方。

---

## 附一 · 真实冒烟花费与快照(阶段 5 · T8)

> 用真实 `deepseek-v4-pro`(并发 1 / 硬上限 6 / 独立 smoke 库,key 仅从 `${DEEPSEEK_API_KEY}` 读)跑通一次完整链路,验证真链路可用并记录受控花费。默认配置为 `并发 3 / 硬上限 16`,真实联调仅此一次。

**讨论快照** —— 话题「远程办公会成为主流吗?」

| 角色 | 姓名 | 职业·Title | 立场 | 颜色 |
|---|---|---|---|---|
| host | 张薇 | 财经评论员 | 中立,重数据趋势 | `#6B7280` |
| expert | 李哲 | 企业战略顾问 | 远程将成主流 | `#2563EB` |
| expert | 王舒怡 | 组织心理学家 | 谨慎怀疑 | `#DB2777` |
| expert | 陈浩然 | 科技公司 CEO | 混合办公务实过渡 | `#16A34A` |

代表性发言(非机械轮流、真对抗):
- 李哲(举手):全面远程可降 30%+ 成本并汇聚全球人才。
- 王舒怡(反驳):成本≠效率,团队远程后创新延期率升 40%,偶遇对话是创意起点。
- 李哲(反驳):创新不必依赖偶遇,结构化流程后创意产出反升 30%。
- 张薇(收尾):自然语言总结、点名双方、无 JSON。

共识/分歧(running 中即入 ticker):〔共识〕远程提效需配套工具与流程 ·〔分歧〕创新是否必须依赖实体偶遇。

**Token 花费(真实)** —— 一场 6 轮 = 7 次调用(P1×1 + P2×5〔含 1 轮引擎合成、不调模型〕+ P3×1):

| | prompt | completion(含 reasoning) |
|---|---:|---:|
| **合计** | **2179** | **2386(1723)** |

折合 ≈ **¥0.047 / 场**(输入 ¥4/1M、输出 ¥16/1M 假设,以账单为准)· 耗时 ~57s · ¥10 可跑 ~200 场同规模讨论。

**红线自检(真实输出):** content 无 JSON ✅ · 无 `reasoning_content`/CoT 泄漏 ✅ · 发言 ≤2 句(最长 75 字)✅ · insight 仅主持人回合且实时 ✅ · key 不入日志/库/文档 ✅。

---

## 附二 · 分阶段实施计划(节选)

> 各阶段先由 `writing-plans` 产出任务计划、经人工闸门确认后再执行。以下为两份主要计划的结构与关键取舍,完整逐任务清单见对应阶段 commit。

**DDD 前端(纯 mock)计划**:T0 脚手架 → {T1 设计令牌 + Fusion Pixel 本地字体、T2 TS 类型对齐 API 契约} → T3 mock 数据 + client(单点集中,便于换真实 fetch)→ T3b `useMockStream`(模拟 SSE 实时感)→ **T4–T9 六组件并行**(每个走 spec 合规审查 → 代码质量审查)→ T10–T12 三页面(演播厅 §6 四区独立滚动 + 三档响应式)→ T13 整合与响应式终验。
关键取舍(ponytail):不引状态管理库(useState/Context)、像素资产纯 CSS(零 PNG)、mock/client 单点可替换。

**阶段 5(Web 层 + E2E)计划**:T1 REST 5 端点 + 统一错误体 → T2 SseHub(每讨论 emitter 组 / 广播 / ~20s 心跳 / 清死连接)+ `/stream` → T3 DiscussionRegistry(ConcurrentHashMap)+ 有界线程池(3)+ 引擎广播 → T4 `confirm→running` 提交引擎 + 重启残留收敛 → **T5 集成测试 A2/A7/A8/A9**(确认交接 / 并行隔离 / snapshot 先行 / 死连接清理)→ T6 前端切真实 `fetch`+`EventSource` → T7 `@Profile("fake-ai")` + Playwright E2E(零花费、确定性)→ T8 真实 Deepseek 冒烟 → T9 verification + 独立代码评审 + README。
关键取舍:SSE 用原生 `SseEmitter`(不引 WebSocket/STOMP)、E2E 跑确定性假 AI 省预算、真实调用只冒烟一次。
