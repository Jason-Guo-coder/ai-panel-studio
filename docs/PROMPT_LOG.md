# 核心 Prompt 记录

> 记录引导 AI 开发的最核心原始 Prompt,按开发范式分阶段。每段含:原始 Prompt 要点 / 意图 / 遇到的挑战 / 如何引导修正。

---

## 第 1 段 · 【引导阶段】需求澄清与过度设计裁剪

**范式定位:** 进入 SDD 前的需求收敛。用 `superpowers:brainstorming`(逐组澄清)+ `ponytail`(砍过度设计)双视角,**一次问一组、等回答再继续**,严格不写任何代码。

**原始 Prompt 要点(引导 AI 的第一条指令):**
> 用 brainstorming 技能,针对 6 个点逐组提问澄清:① MVP 功能边界 ② 核心实体与字段 ③ 实时机制(确认 SSE/SseEmitter 及理由)④ 多讨论并行的状态与事件流隔离 ⑤ 发言调度"非机械轮流"的最小规则 ⑥ AI 编排(多次独立调用 vs 单次生成)。约束:先不写代码、不给完整方案,用 ponytail 视角砍过度设计,一次一组。

**总体意图:** 把作业文档里"画像愿望"与"硬功能要求"分离,先锁边界再谈实现,避免一上来就被大而全的方案带偏、避免 AI 幻觉式堆功能。

**遇到的挑战:** 作业文档信息量大且混入了非硬性诉求(录制回放、多用户等);LLM 天然倾向把画像里的"愿望"当需求实现,造成过度设计。

**如何引导修正:** 用 ponytail 逐条给出"建议砍除清单 + 理由",让人来拍板保留/砍除,而不是 AI 自行发挥;每组结论复述确认后再进下一组,形成可追溯的决策链。

---

### 逐组问答摘要

**① MVP 功能边界**
- **要点/意图:** 分离"必须做(7 项功能要求)"与"明确不做";用 YAGNI 砍掉画像愿望。
- **挑战:** 录制/回放、多用户等"愿望"是否要做,边界模糊。
- **修正结论:** 砍掉多用户账号、人工干预、中途暂停/编辑、专门录制导出、PWA/手势;保留断点续看(先读库再续订 SSE)与三档响应式(打分项);前端一次专注一个讨论;7 项功能要求一项不删。
- **关键立场:** 确立"后端自驱引擎、前端纯观察者"总架构——无人观看后端也推进、写库、SSE 广播。

**② 核心实体与字段**
- **要点/意图:** SDD 骨架,先敲定最小实体集。
- **挑战:** 共识/分歧如何存;瞬时状态是否落库;是否需要事件表。
- **修正结论:** 4 张表(discussion / participant / speech / insight);主持人与专家合表靠 role;共识/分歧合表靠 type + created_at;状态/关注点不落库(内存 + snapshot 事件兜底);不建 event 表;discussion 加 summary 字段独立存主持人总结。

**③ 实时机制 SSE(SseEmitter)**
- **要点/意图:** 确认 SSE 优于 WebSocket 的理由并定落地细节。
- **挑战:** 断线续看、保活、死连接泄漏三个经典坑;SseEmitter 隐含锁定 Spring Boot。
- **修正结论:** 确认栈 = Spring Boot + MyBatis-Plus + SQLite;6 种事件(含 error;status 并入 focus);用 snapshot 兜底替代 Last-Event-ID 重放;前端按 seq/created_at 去重排序消除竞态;~20s :ping 心跳 + onCompletion/onTimeout/onError 移除死连接。

**④ 多讨论并行隔离**
- **要点/意图:** 用最小并发模型隔离状态与事件流。
- **挑战:** 隔离边界如何定;线程安全;重启后 running 讨论怎么办;并发写状态。
- **修正结论:** `ConcurrentHashMap<Long, DiscussionSession>` 以 id 为隔离边界;有界线程池并发上限 3(可配);emitters 用 CopyOnWriteArrayList;重启把 running 标 interrupted(前端显"已中断",历史照看);单写者纪律 + 确认为交接点;SQLite 开 WAL。

**⑤ 发言调度"非机械轮流" + 终止条件**
- **要点/意图:** 在"可见的调度逻辑(打分项)"与"预算(¥10)"间取平衡。
- **挑战:** 纯 LLM 决定谁说会让调度逻辑不可见(踩打分项);完整竞价 N 调用/轮烧钱。
- **修正结论:** 混合模式——"选谁"由内容相关性驱动(非机械),Java 只做硬规则校验(不能连说、反驳须有 target、主持人节奏、上限收尾)+ 防退化;目标 1 调用/轮输出 `{说话人, 反应类型, target, 1-2句}`;reaction_type + target_speech_id 仅调度用不渲染;硬上限默认 16 条(可配)作为预算护栏。

**⑥ AI 编排(多次独立调用 vs 单次生成)**
- **要点/意图:** 收口全系统 AI 调用类型与"共识提炼放哪"。
- **挑战:** 独立提炼调用翻倍烧钱;融进 P2 又担心 schema 过大/可测性。
- **修正结论:** 全系统仅 3 种 Prompt(P1 阵容 / P2 每轮 / P3 总结);共识/分歧融进 P2 但**仅主持人回合(串联)填充**,专家回合留空;小窗只给 P2 点名者真实 focus,其余待机;失败降级——重试 1 次→仍败强制切主持人回合续跑→连续崩才 error+暂停。

---

---

## 第 2 段 · 【SDD 阶段】数据建模与 API 契约生成

**范式定位:** 契约/模型驱动。把引导阶段结论展开为交付级文档,**分步产出、不写实现代码**。

**原始 Prompt 要点:**
> 进入 SDD,产出交付级文档。不要压成一份内部设计稿,展开为:`docs/PRD.md`(定位/痛点/MVP 范围/页面清单/核心流程 mermaid flowchart/技术方案/风险带缓解)、`docs/architecture.md`(技术栈/目录结构/erDiagram 含 discussion_id 隔离字段/服务层/AI 编排/红线清单/验收标准)、`db/schema.sql` + `db/seed.sql`(≥5 条样例)+ `docs/API.md`(REST + SSE 事件契约)。技术栈按已定:React+Vite+TS / Spring Boot+MyBatis-Plus+SQLite / SSE。此步不写实现代码,分步来。

**意图:** 用契约先钉死"字段名/状态枚举/事件类型",让后续前后端实现只填空、不发明,压制 AI 幻觉。

**遇到的挑战与如何引导修正:**
1. **mermaid 渲染坑**:PRD 核心流程 flowchart 节点用了 `\n` 换行,部分渲染器不认。引导:erDiagram 不动,只把 flowchart 节点 `\n` 全改 `<br/>`。
2. **契约自相矛盾**:architecture §6 与 API §2 标题写"6 种事件",实际列了 7 行(`summary`、`finished` 是两个事件)。引导:两处统一改"7 种事件"(BRAINSTORM/PROMPT_LOG 的表把 summary+finished 合一行数,语境自洽,不动)。
3. **措辞不准导致误读一致性**:声称专家色板"与 seed 对齐",实际只讨论 1 对上,讨论 2–5 用了色板外手选色。引导:改准措辞——8 色板仅用于**新讨论 P1 生成**(`expertIndex % 8`),`color` 是 participant 存储列按存值渲染,seed 历史用同色系手选色、不强制落在 8 色内(**不改 seed 数据**)。
4. **补漏**:PRD 增补"核心用户旅程"(发起者/观察者双旅程),对齐打分项。
5. 用 SQLite 临时库灌 schema+seed 跑 `PRAGMA foreign_key_check`,确认 5 讨论/25 参会者/28 发言/10 共识分歧、悬空反驳 0,契约层自检通过。

---

## 第 3 段 · 【DDD 阶段】前端组件与页面生成

**范式定位:** 设计驱动。先 `design.md`(全像素风·圆桌像素演播厅)定视觉/交互基线,再脚手架 + 六组件 + 三页面,**全用 mock、不接后端**。

**原始 Prompt 要点(设计基线):**
> 视觉方向:全像素风·圆桌像素演播厅。先给章节提纲我确认再成文:主色调(深底+克制像素板+LIVE 红+专家专属色+可选 CRT 扫描线)、字体(Zpix vs Fusion Pixel 12,含可读性红线)、六组件 props/变体、关键交互(聚光灯/像素声波/三态动效/运动安全)、三态视觉(信号接入中/空台/雪花屏)、布局与三档响应式。

**原始 Prompt 要点(实现):**
> 用 subagent-driven-development,脚手架 frontend(React+Vite+TS),设计令牌落地(CSS variables + Fusion Pixel 本地 woff2),TS 类型对齐 API DTO 与 7 种 SSE payload,mock 数据仿 seed 集中管理便于替换;并行分派六组件,每个走 spec 合规审查→代码质量审查→commit;三页面(首页/嘉宾确认/演播厅);不引状态库,useState/Context 足够。

**意图:** 让"演播厅沉浸感/实时感"先在纯前端跑起来可视验收;设计令牌与类型契约先行,组件叶子优先、页面组合在上,便于并行与替换真实接口。

**遇到的挑战与如何引导修正:**
1. **中文像素字体选型**:Zpix 更"硬"像素但 CJK 覆盖有限。引导:默认 **Fusion Pixel 12**(全 CJK,话题/人名不出豆腐块;OFL 可本地 woff2 内嵌),Zpix 作回退;summary 长文用清晰字体回退保可读。
2. **断点数字不一致**:design.md §6 窄屏 ASCII 标 `<768px`,断点令牌却是 `<1024`。引导:统一 `<1024` 单栏,与 `--bp-desktop:1024` 一致。
3. **演示态与真实 seed 的边界**:首页要出 🔴LIVE 进行中卡,但 seed 全是 finished。引导:在 **mock client 里加 `DEMO_STATUS`** 覆盖(disc1/2→running、disc5→interrupted),**不改 `mockData`/`db/seed.sql`**;真实后端接入后删 `DEMO_STATUS` 即可。
4. **跨区块布局 bug(实测)**:演播厅主席台区内容超高,"准备发言"的紫色专家小窗**溢出渲染到下方绿色共识框**,重合。引导:先给 stage 区 `overflow:hidden` 裁剪→发现主持人卡片底部被硬切→再改为 **stage 整区滚动**(`overflow-y:auto`),卡片完整且不溢出到相邻区。
5. **并行编排**:六组件独立叶子,用 subagent 并行分派,回收后统一 tsc/build + spec/质量双审查再逐个 commit,避免并发 build/git 竞态。

---

---

## 第 4 段 · 【TDD 阶段】核心逻辑测试用例与业务实现

**范式定位:** 测试驱动。先给"测试清单"确认覆盖,再严格 RED→GREEN→REFACTOR,靠实现转绿、绝不改断言凑绿。

**原始 Prompt 要点(先清单后 RED):**
> 用 test-driven-development,先只给测试清单(不写实现/测试代码),覆盖三块核心逻辑:A 嘉宾生成(1主持+N专家/颜色互异/立场有别/字段完整/人数边界)、B 发言调度(内容驱动非轮流/反驳须有效target/不许连说·补充自己例外/1–2句/主持人节奏/硬上限16/失败降级三级)、C 共识提炼(仅主持人回合/解析去重入库/实时)。每条列测试名·输入·期望断言。AI 一律 Mockito 打桩,零网络。确认后进 RED。

**原始 Prompt 要点(GREEN 纪律):**
> 逐单元 RED→GREEN→REFACTOR,每单元跑绿即 commit。靠实现让测试通过,绝不改断言凑绿;测试语义有误先停下问,不静默改测试。实现真实 DeepseekClient(P1/P2/P3 prompt + JSON 解析 + 从 env 读 key),但单测仍 Mockito 打桩,真实调用留后阶段。

**意图:** 用测试把"非机械轮流/失败降级/仅主持人回合产出"这些易出错逻辑钉死,避免大模型幻觉式实现。契约先行(桩)让 33 条测试先编译后全红,证明测试确实在测目标行为。

**遇到的挑战与如何引导修正:**
1. **补红线缺口**:清单确认时新增两条——`补充` 例外收窄到"补充自己上一句"(否则补充成绕过连说的后门)、`speakerId 不在阵容`须判非法触发降级。防止规则被钻空。
2. **A2 颜色语义澄清**:颜色一律后端按调色板 index 指派、不信任 LLM 配色(P1 可不出颜色)——测试断言"服务指派后专家色互异",而非校验 LLM 输出。
3. **MyBatis-Plus 3.5.9 依赖坑**:`PaginationInnerInterceptor` 拆到独立 `mybatis-plus-jsqlparser` 依赖,编译报"找不到符号";补依赖解决。
4. **RED 全红**:33 条测试因桩 `UnsupportedOperationException` 全部失败(4 Failures + 29 Errors),每条断在被测方法上——证明非编译错、非笔误。逐单元实现后 GREEN。

---

## 第 5 段 · 【E2E 阶段】系统级端到端测试与质量闭环

**范式定位:** 质量闭环。建 Web 层(REST + SSE + 引擎真跑),前端切真实接口,用"假 AI"跑确定性 E2E,真实 Deepseek 只做一次小成本冒烟。

**原始 Prompt 要点:**
> 建 web 层打通端到端:REST(列表/创建P1/重生成/confirm→running提交引擎/详情)+ SSE(SseHub 每讨论 emitter 组/广播/~20s心跳/onCompletion/onTimeout/onError 清死连接)+ DiscussionRegistry(ConcurrentHashMap)+ 有界线程池(3)+ 引擎循环真跑;新连接先 snapshot 再实时;单写者交接;启动 WAL+残留 running 标 interrupted。集成测试补 A2/A7/A8/A9。前端切真实 fetch+EventSource、删 mockData/useMockStream/DEMO_STATUS。E2E 跑在 fake-ai profile(零 Deepseek 花费、确定性),真实 Deepseek 只手动冒烟一次(并发1/上限6/记花费,DEEPSEEK_MODEL 设真实 V4 Pro id)。

**意图:** 端到端闭环验证"沉浸感/实时感"真的落地;用 fake-ai 把 E2E 做成确定性、可重复、零花费、不 flaky,把真实 AI 花费压到一次冒烟。

**遇到的挑战与如何引导修正(真实踩坑):**
1. **SQLite `speech.id=seq` 主键碰撞(集成测试逮到的生产级隐患)**:引擎 `persist()` 把 `seq` 手设成主键 → 不同讨论/累积库里 `speech.id` 必撞(`SQLITE_CONSTRAINT_PRIMARYKEY`)。**单测发现不了**(mock 了 mapper),是集成测试 A7(两讨论并行)才暴露。修:不设 id、让 MyBatis-Plus AUTO 回填真实自增 id。用 systematic-debugging 查异常栈定位,未盲改。
2. **测试隔离**:集成库初用 `:memory:?cache=shared`,`journal_mode=memory`(非 WAL)导致读阻塞写、上下文关闭时内存库被销毁 → 引擎线程写超时/失败。换成 `target` 文件库 + WAL(读不阻塞写、不销毁)。
3. **SSE 测试方式**:MockMvc 对 `SseEmitter` 的 `getAsyncResult/asyncDispatch` 不适用("async result not set")。改用真 HTTP(A8 读首个事件=snapshot)+ 直接 hub(A9 经广播失败清理路径)。
4. **E2E 预算策略**:`@Profile("fake-ai")` 的 `FakeAiService` 确定性产出合法 P1/P2/P3;`DeepseekAiService` 加 `@Profile("!fake-ai")` 避免双 bean。错误态用 `__FAIL__` 话题钩子确定性触发。E2E 零花费、可重复。
5. **v4-pro 推理模型成本/延迟**:真实冒烟一场 6 轮讨论 = 7 次调用、prompt 2179 + completion 2386(含 reasoning 1723)tokens ≈ ¥0.05、约 57s。给 DeepseekClient 加 usage 日志(分列 prompt/completion/reasoning)+ 180s 读超时;只取 `message.content`,不读 `reasoning_content`(CoT 不泄漏)。详见 `docs/WORKFLOW.md` 附一。

