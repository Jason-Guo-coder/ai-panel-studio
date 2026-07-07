# AI 圆桌讨论 MVP · 视觉与交互设计基线(Design)

> DDD 阶段设计驱动文档。视觉方向:**全像素风 · 圆桌像素演播厅**。本文是前端组件与状态流转的设计事实源,上承 `PRD.md`/`architecture.md`。
> 本文给设计令牌(CSS variables)、组件契约(props/变体)与动效规格(CSS 草图),**不含实现代码**。

---

## §0 设计原则与像素隐喻

1. **沉浸感 / 实时感第一**:界面要像在看一场直播演播厅——LIVE 信号、聚光灯、声波、滚动 transcript。
2. **像素克制统一**:有限调色板、统一像素网格(基准 12px),不堆花哨、不上刺眼彩虹。
3. **可读性红线高于风格**(打分项 + 无障碍):正文对比度/字号达标,绝不为像素感牺牲 legibility;运动尊重 `prefers-reduced-motion`。
4. **资产优先 CSS**(ponytail):像素头像、声波、扫描线、雪花屏均用 CSS 实现,尽量零 PNG。

---

## §1 主色调与调色板

### 舞台三层背景(深色演播厅底)
| Token | 值 | 使用场景 |
|---|---|---|
| `--bg-stage` | `#0D0F14` | 演播厅最底舞台色(page 根背景) |
| `--bg-panel` | `#161A22` | 四大区面板底色 |
| `--bg-elevated` | `#1E2430` | 讨论卡 / 状态小窗 / 名条卡片 |
| `--border` | `#2A3140` | 像素边框(默认) |
| `--border-strong` | `#3A4356` | 强调边框 / 分隔 |

### 文本(对比度达标)
| Token | 值 | 场景 | 对比(on `--bg-panel`) |
|---|---|---|---|
| `--text-primary` | `#E6EDF3` | 正文 / 姓名 | ~13:1 ✅ AAA |
| `--text-secondary` | `#9AA4B2` | Title / 次要 | ~5.3:1 ✅ AA |
| `--text-dim` | `#6B7480` | 占位 / 待机态 | 仅用于非正文 |

### LIVE 信号红(高饱和,专用)
| Token | 值 | 场景 |
|---|---|---|
| `--live-red` | `#FF3B30` | 🔴 LIVE 徽标 / 直播信号,**仅此用途**,不作普通强调色 |
| `--live-glow` | `rgba(255,59,48,.45)` | LIVE 呼吸辉光 |

### 共识 / 分歧语义色
| Token | 值 | 场景 |
|---|---|---|
| `--consensus` | `#3FB950` | 共识 ticker(绿) |
| `--divergence` | `#F0883E` | 分歧 ticker(橙) |

### 专家专属色规则(用于 P1 新讨论生成)
- **固定有序调色板**,**仅用于新讨论 P1 生成时**按入场序号取色:`palette[expertIndex % 8]`。
- `color` 是 `participant` 表存储列,前端一律**按存值渲染、不受本调色板约束**;`seed.sql` 历史讨论用的是同一深底友好色系内的手选色,**不强制落在这 8 色内**(故无需改 seed 数据)。
```
1 #2563EB  蓝     5 #7C3AED  紫
2 #DB2777  品红   6 #0891B2  青
3 #16A34A  绿     7 #EA580C  橙
4 #F59E0B  琥珀   8 #0D9488  teal
```
- 色相拉开、饱和度中等(适配深底);相邻小窗不撞色。
- 每位专家的 `color` 用于:头像卡边框、状态小窗描边、发言名条左侧色块。**同一人全局同色**,建立"色 = 人"的心智映射。
- **主持人不参与此调色板**,固定中性灰 `#6B7280`(见 §3 主席台差异化),视觉上与专家区隔。

### 可选 CRT 扫描线叠加(纯 CSS,可关)
- 全屏 overlay:`repeating-linear-gradient(0deg, rgba(0,0,0,.22) 0 1px, transparent 1px 3px)`,`pointer-events:none`。
- 默认开启、提供开关;`prefers-reduced-motion` 下**静态不动/减弱**(见 §4)。

---

## §2 字体系统

### 选型:默认 Fusion Pixel 12,Zpix 备选
- **正文/CJK 主字体 `Fusion Pixel 12`**:全 CJK 覆盖(话题/人名不出豆腐块)、多字号、OFL 许可可**本地 woff2 内嵌**,不依赖 CDN。
- **备选/回退 `Zpix`**:更"硬"像素感,作二级回退。
- **清晰字体回退栈**(像素字失效或长文可读性不足时):
```css
--font-pixel: "Fusion Pixel 12", "Zpix", "PingFang SC", "Microsoft YaHei", sans-serif;
--font-readable: "PingFang SC", "Microsoft YaHei", system-ui, sans-serif;
```

### 用途分层
| 用途 | 字体 | 字号 | 说明 |
|---|---|---|---|
| UI 标签 / 徽标 | `--font-pixel` | 12px | LIVE、状态词、按钮 |
| 姓名 / Title | `--font-pixel` | 12–14px | 名条、头像卡 |
| 标题 | `--font-pixel` | 16–20px | 区块标题(12 的整数倍,像素不糊) |
| **Transcript 正文** | `--font-pixel` 14px / 行高 1.6 | **≥12px 红线** | 若像素字在长句下 legibility 不足,正文切 `--font-readable` |
| **主持人总结** | 16px(可 `--font-readable`) | 全文最长文本 | 舒适度优先,字号更大 / 允许清晰字体 |

### 可读性红线(不可简化)
- 正文对比度 **≥ WCAG AA 4.5:1**(已由 §1 文本色保证)。
- 正文最小 **12px**,推荐 14px;summary 16px。
- 像素字仅用于**短字符串**(标签/姓名/标题);长段落(transcript / summary)可用清晰字体回退,**风格让位可读性**。
- 字体文件本地内嵌,`font-display: swap`,避免加载期空白。

---

## §3 组件库(复用)

> 每个组件列 **props + 变体**。像素资产用 CSS(box-shadow 像素块 / clip-path),不用 PNG。

### 3.1 讨论卡 `DiscussionCard`(首页列表)
| props | 类型 | 说明 |
|---|---|---|
| topic | string | 话题 |
| status | `running`\|`finished`\|`interrupted` | 状态 |
| expertCount | number | 人数 |
| createdAt | string | 时间 |
| onEnter | () => void | 进入观看 |

**变体(status):**
- `running` → 右上 🔴LIVE 徽标(呼吸);边框 `--border-strong`。
- `finished` → 灰"✓ 已结束"标;低饱和。
- `interrupted` → 像素"信号断裂"标 + `--text-dim`。

### 3.2 嘉宾像素头像卡 `PixelAvatarCard`(确认页 / 主席台）
| props | 类型 | 说明 |
|---|---|---|
| name / profession / title / stance | string | 人设 |
| color | string | 专属色(专家);主持人忽略,用中性灰 |
| role | `host`\|`expert` | 决定差异化处理 |

- **像素头像(CSS 绘制)**:方形像素头 + 差异化特征——西装翻领、眼镜(有/无)、发型/发色区块,保证辨识度。
- **专家变体**:头像外框 = `color`;底部色块名条。
- **主持人变体(差异化,不与专家混排)**:
  - 中性灰 `#6B7280` 描边 + **"主持"像素徽标**;
  - 卡片更宽/居中,置于**主席台**位(§6 布局中央顶部),视觉高于专家一档;
  - 不进专家状态小窗网格。

### 3.3 专家状态小窗 `ExpertStatusWindow`
| props | 类型 | 说明 |
|---|---|---|
| participant | {name,title,color} | 专家 |
| status | `待机`\|`准备发言`\|`发言中` | 三态 |
| focus | string \| null | 公开关注点(仅准备/发言中有) |
| isCurrentSpeaker | boolean | 聚光灯高亮 |

**变体(status → 见 §4 动效):**
- `待机` idle bob;边框 `--border`;focus 隐藏。
- `准备发言` bounce;边框 = `color`;显 focus。
- `发言中` 嘴部动画 + 聚光灯 + 像素声波;边框高亮 `color`。
> 仅专家进此组件;主持人不在此(在主席台)。

### 3.4 发言像素名条 `SpeechNameplate`(Transcript 行)
| props | 类型 | 说明 |
|---|---|---|
| name / title | string | 发言人 |
| color | string | 左侧色块(主持人 = 中性灰) |
| content | string | 发言正文 |
| isHost | boolean | 主持人样式微区隔 |

- 左侧 4px 像素色块 = 发言人色;姓名·Title 用像素字;正文用 §2 正文规则。
- **红线 L6**:不渲染 `reaction_type`/"举手"等内部事件,只显姓名·Title·内容。
- 主持人行:中性灰块 + 轻微区隔(如"主持"小标),不与专家色混淆。

### 3.5 共识/分歧像素 ticker `InsightTicker`
| props | 类型 | 说明 |
|---|---|---|
| items | {type,content,createdAt}[] | 增量条目 |
| type 分栏 | consensus / divergence | 双栏/双色 |

- 像素 LED 板质感;**共识绿 / 分歧橙**(§1)。
- 双区:共识栏 + 分歧栏,各自新条目**从顶部滑入**(§4),按 `createdAt` 序。
- 变体:`consensus`(绿描边+✓像素图元)/ `divergence`(橙描边+⚡像素图元)。

### 3.6 主持人总结区 `SummaryPanel`
| props | 类型 | 说明 |
|---|---|---|
| summary | string \| null | 自然语言总结 |
| loading | boolean | 生成中 |

- "主持人总结"像素标题 + 中性灰主持人色调 accent。
- 正文用 §2 summary 规则(16px / 可清晰字体),舒适可读。
- **红线 L10**:只渲染自然语言,**禁 JSON 原文上屏**;未结束显空态(§5)。

---

## §4 关键交互动作(优先 CSS `steps()`,无图资产)

### 4.1 当前发言人聚光灯 + 边框高亮 + 像素声波
- 聚光灯:`box-shadow: 0 0 0 2px var(--speaker-color), 0 0 24px var(--speaker-glow)`,慢呼吸。
- 声波:3–5 根像素竖条,高度用 `steps()` 逐帧跳变(非平滑),模拟像素音柱。
```css
@keyframes wave { 0%{height:4px} 50%{height:14px} 100%{height:4px} }
.bar{ animation: wave .6s steps(3) infinite; }
```

### 4.2 状态三态动效
| 态 | 动效 | 规格 |
|---|---|---|
| 待机 idle | 轻微上下 bob | `translateY` 0↔2px,`steps(2)`,~2s |
| 准备 prepare | 弹跳 bounce | `translateY` 0↔-4px 快弹,~.5s |
| 发言中 speaking | 嘴部开合 | 像素嘴元素两态 `steps(2)`,~.3s 循环 |

### 4.3 🔴 LIVE 闪烁
- `opacity` 1↔.35 `steps(2)` ~1s;辉光 `--live-glow`。

### 4.4 新发言滚动进入
- 新 `SpeechNameplate`:`translateY(8px)+opacity 0 → 0/1`,~.25s;transcript 容器自动滚到底。

### 4.5 运动安全(无障碍红线,不可简化)
```css
@media (prefers-reduced-motion: reduce) {
  /* 关闭/减弱:CRT 扫描线动、LIVE 闪烁、idle bob、嘴部动画、声波 */
  *{ animation: none !important; }
  .crt-overlay{ opacity:.12; }            /* 扫描线转静态、更淡 */
  .live-badge{ opacity:1; }               /* 闪烁→常亮红点 */
  .speaking{ border-color: var(--speaker-color); } /* 用静态高亮替代动画表达"发言中" */
}
```
> 关键:去掉动画后,**状态仍能靠静态样式(边框/常亮红点/色块)区分**,信息不丢失。

---

## §5 三态视觉(逐区域,像素/CRT 隐喻)

| 区域/场景 | 加载中 | 空数据 | 错误 |
|---|---|---|---|
| **首页列表** | 像素"信号接入中" + 转圈砖块 spinner | 像素"演播厅空台"占位:"暂无进行中的圆桌,发起一场?" | 雪花屏 + "列表加载失败" + 重试 |
| **演播厅接入** | "📡 信号接入中…"像素条 | "信号已接入,等待开场…"空台 | "🔌 信号中断"雪花屏 + 重试 |
| **AI 失败(SSE error 事件)** | — | — | 该区亮"信号中断",提示"AI 调用失败,请重试" |
| **SSE 断连** | 重连中像素提示 | — | 多次失败→雪花屏 + 手动重连 |

- **转圈砖块 spinner**:4 块像素方块顺时针点亮,`steps(4)`。
- **雪花屏**:CSS 噪点(多层 `repeating-linear-gradient` / 伪随机 box-shadow 像素点)+ 轻抖动;`reduced-motion` 下静态噪点。
- **空台占位**:像素舞台线框 + 居中提示,呼应演播厅隐喻。

---

## §6 演播厅布局与三档响应式(打分项 + 硬约束)

### 四大区
- **A · 现场 Transcript**(主视觉,最宽)
- **B · 主席台 + 专家状态小窗组**(主持人主席台居中顶部 + 专家小窗网格)
- **C · 共识 / 分歧 ticker**
- **D · 主持人总结区**

### 硬约束
- **无整页滚动**:`html,body{ overflow:hidden; height:100vh }`;用 CSS Grid 分区。
- **各区独立滚动**:每区为独立滚动容器(`overflow:auto` + `min-height:0` 让 grid 子项可收缩滚动)。
- LIVE 信号条常驻顶部。

### 断点排布

**超宽屏 ≥1600px(三栏)**
```
┌───────────┬────────────────────┬───────────┐
│ B 主席台   │  A  Transcript      │ C 共识/分歧│
│  +专家小窗 │  (最宽,独立滚动)    │ (独立滚动) │
│ (独立滚动) │                     ├───────────┤
│           │                     │ D 主持人总结│
└───────────┴────────────────────┴───────────┘
```

**桌面 1024–1599px(两栏)**
```
┌───────────────┬──────────────┐
│ A Transcript   │ B 主席台+小窗 │
│ (最宽,独立滚) ├──────────────┤
│               │ C 共识/分歧   │
│               ├──────────────┤
│               │ D 总结        │
└───────────────┴──────────────┘
```

**窄屏 <1024px(单栏纵向堆叠,各区仍独立滚动)**
```
┌──────────────┐
│ 🔴LIVE + 主席台│
│ 专家小窗(横向滚)│  ← 小窗一行横向滚动
├──────────────┤
│ A Transcript  │  ← 固定高度,独立滚动
├──────────────┤
│ C 共识/分歧    │  ← 独立滚动
├──────────────┤
│ D 主持人总结   │
└──────────────┘
```

### 断点令牌
```css
--bp-desktop: 1024px;
--bp-wide: 1600px;
/* <1024 窄屏单栏;1024–1599 两栏;≥1600 三栏 */
```

---

## 附:设计红线速查(对齐 architecture 红线清单)
- L6:transcript 不渲染 `reaction_type`/内部事件 → §3.4
- L10:summary 只显自然语言,禁 JSON → §3.6
- 可读性红线:正文 ≥12px / 对比 ≥4.5:1 → §2
- 无障碍红线:`prefers-reduced-motion` 关键动效可关且状态不丢 → §4.5
- 硬约束:无整页滚动 + 各区独立滚动 + 三档响应式 → §6
- 主持人视觉区隔:中性灰 + 主席台 + "主持"标,不与专家混排 → §1/§3.2

---

*(下一步:DDD → TDD 阶段,针对嘉宾生成/发言调度/共识提炼写测试用例与业务实现。)*
