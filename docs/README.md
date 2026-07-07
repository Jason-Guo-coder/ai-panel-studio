# 开发文档索引

> 本项目按 **引导 → SDD → DDD → TDD → E2E → 收尾** 多范式分阶段开发,下列文档为各阶段产物。
> 建议阅读顺序:`PRD` → `architecture` → `API` → `design`,再看 `PROMPT_LOG` / `WORKFLOW` 了解过程与决策。

| 文档 | 阶段 / 范式 | 作用 |
|---|---|---|
| [PRD.md](PRD.md) | 引导 → **SDD** | 产品需求:定位 / 痛点 / MVP 范围 / 页面清单 / 核心流程(mermaid)/ 技术方案 / 风险 |
| [architecture.md](architecture.md) | **SDD** | 技术架构:技术栈 / 目录 / 数据模型(ER mermaid)/ 服务层约定 / AI 编排 / 红线清单(L1–L10)/ 验收标准(A1–A12) |
| [API.md](API.md) | **SDD** | 接口契约:REST 5 端点 + **7 种** SSE 命名事件 + 统一错误体 |
| [design.md](design.md) | **DDD** | 视觉 / 交互基线:全像素风演播厅——调色板 / 字体(可读性红线)/ 六组件 / 三态 / 三档响应式 |
| [BRAINSTORM.md](BRAINSTORM.md) | 引导 | 需求澄清收敛结论(六组),后续 SDD/DDD/TDD 的事实源头 |
| [PROMPT_LOG.md](PROMPT_LOG.md) | 全程(交付物 2) | 核心 Prompt 记录 **5 段**,四范式齐全:每段原始 Prompt + 意图 + 挑战 + 如何引导修正 |
| [WORKFLOW.md](WORKFLOW.md) | 全程(交付物 3) | 开发过程思路 & 工作流:流程 / 3 个典型问题及解决 / 对"工程化 AI 开发"的理解 |
| [SMOKE.md](SMOKE.md) | **E2E** | 真实 `deepseek-v4-pro` 冒烟记录:讨论快照 + token 花费(分列)+ 红线自检 |

**数据库脚本**:[`../db/schema.sql`](../db/schema.sql)(建表 + 索引 + WAL)· [`../db/seed.sql`](../db/seed.sql)(≥5 场样例讨论,`INSERT OR IGNORE` 幂等)

**运行指南 / 环境变量 / 技术选型 / API 列表** 见项目根 [`../README.md`](../README.md)。
