// Mock 数据 —— 忠实移植 db/seed.sql 的 5 场讨论(话题+阵容+transcript+共识分歧+总结)。
// 集中此处;页面/组件不得散落硬编码。后续替换真实接口只需改 client.ts。

import type { DiscussionDetail } from '../types/dto'

export const mockDiscussions: DiscussionDetail[] = [
  {
    discussion: {
      id: 1, topic: 'AI 是否会取代初级程序员?', status: 'finished', expertCount: 4,
      createdAt: '2026-07-01 10:00:00',
      summary:
        '本场没有落到"取代/不取代"的简单结论,而是收敛出更有价值的共识:AI 会吃掉重复性编码,但需求理解、系统设计与判断力仍是人的护城河。分歧集中在初级岗位是数量萎缩还是升级为"AI 协作者"。对个人的启示是把能力重心从"写代码"移向"定义问题与验收结果"。',
    },
    participants: [
      { id: 1, role: 'host', name: '林知远', profession: '科技媒体人', title: '圆桌主持', stance: '中立引导', color: '#6B7280' },
      { id: 2, role: 'expert', name: '陈默', profession: '软件架构师', title: '首席架构师', stance: '部分替代不可避免', color: '#2563EB' },
      { id: 3, role: 'expert', name: '苏晴', profession: '计算机教育学者', title: '副教授', stance: '教育需转型而非消亡', color: '#DB2777' },
      { id: 4, role: 'expert', name: '王砚', profession: 'AI 创业者', title: 'CEO', stance: '工具增强而非取代', color: '#16A34A' },
      { id: 5, role: 'expert', name: '赵磊', profession: '一线开发者', title: '高级工程师', stance: '低估了工程复杂性', color: '#F59E0B' },
    ],
    speeches: [
      { id: 1, participantId: 1, content: '今天聊一个焦虑话题:AI 会不会取代初级程序员?先请各位亮明立场。', reactionType: '开场', targetSpeechId: null, seq: 1, createdAt: '2026-07-01 10:00:05' },
      { id: 2, participantId: 2, content: '重复性的 CRUD 和样板代码会被大量自动化,初级岗位的传统形态确实会萎缩。', reactionType: '举手', targetSpeechId: null, seq: 2, createdAt: '2026-07-01 10:00:20' },
      { id: 3, participantId: 5, content: '可真实项目里八成难点是需求歧义和系统耦合,这些 AI 现在接不住。', reactionType: '反驳', targetSpeechId: 2, seq: 3, createdAt: '2026-07-01 10:00:38' },
      { id: 4, participantId: 4, content: '所以关键词是"增强"——初级开发者配上 AI 会变成"超级初级",产出翻倍而非消失。', reactionType: '补充', targetSpeechId: null, seq: 4, createdAt: '2026-07-01 10:00:55' },
      { id: 5, participantId: 1, content: '听起来分歧在于"岗位消失"还是"岗位升级"。苏老师,从教育端看呢?', reactionType: '串联', targetSpeechId: null, seq: 5, createdAt: '2026-07-01 10:01:10' },
      { id: 6, participantId: 3, content: '教育必须从"教语法"转向"教判断与提问",否则培养的正是最易被替代的那批人。', reactionType: '举手', targetSpeechId: null, seq: 6, createdAt: '2026-07-01 10:01:28' },
      { id: 7, participantId: 1, content: '感谢各位,我们把碰撞收在这里。', reactionType: '收尾', targetSpeechId: null, seq: 7, createdAt: '2026-07-01 10:01:45' },
    ],
    insights: [
      { id: 1, type: 'consensus', content: '重复性、样板化的编码工作会被 AI 大幅自动化。', createdAt: '2026-07-01 10:01:12' },
      { id: 2, type: 'divergence', content: '初级岗位走向"消失"还是"升级为 AI 协作者",各方判断不同。', createdAt: '2026-07-01 10:01:13' },
    ],
  },
  {
    discussion: {
      id: 2, topic: '远程办公 vs 回归办公室,哪个是未来?', status: 'finished', expertCount: 4,
      createdAt: '2026-07-02 14:00:00',
      summary:
        '各方共识是"一刀切"最糟——无论强制到岗还是全面远程都会牺牲一部分人的产出。真正的分歧在于混合办公的重心该偏向灵活性还是协作密度。结论倾向于:以团队协作节奏而非管理者偏好来决定到岗规则。',
    },
    participants: [
      { id: 6, role: 'host', name: '周衡', profession: '职场观察员', title: '圆桌主持', stance: '中立引导', color: '#6B7280' },
      { id: 7, role: 'expert', name: '何蓉', profession: '组织行为学者', title: '教授', stance: '协作密度不可替代', color: '#7C3AED' },
      { id: 8, role: 'expert', name: '李维', profession: '远程公司创始人', title: 'CEO', stance: '远程是效率解放', color: '#0891B2' },
      { id: 9, role: 'expert', name: '张岚', profession: 'HR 负责人', title: 'HRD', stance: '混合办公是现实解', color: '#DC2626' },
      { id: 10, role: 'expert', name: '吴迪', profession: '一线工程经理', title: '研发经理', stance: '看团队而非看口号', color: '#65A30D' },
    ],
    speeches: [
      { id: 8, participantId: 6, content: '远程还是回办公室,吵了好几年了。今天不喊口号,只谈什么情况下用什么。', reactionType: '开场', targetSpeechId: null, seq: 1, createdAt: '2026-07-02 14:00:05' },
      { id: 9, participantId: 8, content: '远程把通勤和无效会议的时间还给了员工,深度工作反而更多。', reactionType: '举手', targetSpeechId: null, seq: 2, createdAt: '2026-07-02 14:00:22' },
      { id: 10, participantId: 7, content: '但创新常发生在走廊偶遇里,纯远程会悄悄杀死这种非正式协作。', reactionType: '反驳', targetSpeechId: 9, seq: 3, createdAt: '2026-07-02 14:00:40' },
      { id: 11, participantId: 10, content: '我的经验是:执行期远程高效,探索期必须面对面,不该用一个规则套所有阶段。', reactionType: '补充', targetSpeechId: null, seq: 4, createdAt: '2026-07-02 14:00:58' },
      { id: 12, participantId: 9, content: '所以混合不是妥协,是把到岗决定权交给团队协作节奏,而非管理者打卡偏好。', reactionType: '举手', targetSpeechId: null, seq: 5, createdAt: '2026-07-02 14:01:16' },
      { id: 13, participantId: 6, content: '很好,分歧收敛到"谁来定到岗规则"上了,先收在这。', reactionType: '收尾', targetSpeechId: null, seq: 6, createdAt: '2026-07-02 14:01:32' },
    ],
    insights: [
      { id: 3, type: 'consensus', content: '"一刀切"的到岗政策会牺牲部分人的产出,是最差选项。', createdAt: '2026-07-02 14:01:18' },
      { id: 4, type: 'divergence', content: '混合办公的重心应偏向个人灵活性,还是团队协作密度。', createdAt: '2026-07-02 14:01:19' },
    ],
  },
  {
    discussion: {
      id: 3, topic: '城市应不应该全面禁售燃油车?', status: 'finished', expertCount: 4,
      createdAt: '2026-07-03 09:30:00',
      summary:
        '讨论共识是方向正确但节奏不能激进,电网、充电基建与低收入群体的承受力是硬约束。分歧在于"设定硬性禁售时间表"还是"用市场与配套自然淘汰"。落点建议是给出明确时间表但配套阶梯式补贴与基建先行。',
    },
    participants: [
      { id: 11, role: 'host', name: '许明', profession: '公共政策评论员', title: '圆桌主持', stance: '中立引导', color: '#6B7280' },
      { id: 12, role: 'expert', name: '郑清', profession: '环境经济学家', title: '研究员', stance: '禁售刻不容缓', color: '#059669' },
      { id: 13, role: 'expert', name: '孙昊', profession: '汽车产业分析师', title: '首席分析师', stance: '产业转型需缓冲', color: '#2563EB' },
      { id: 14, role: 'expert', name: '马丽', profession: '电网工程师', title: '高级工程师', stance: '基建是前置条件', color: '#EA580C' },
      { id: 15, role: 'expert', name: '冯涛', profession: '社会政策学者', title: '副教授', stance: '警惕转嫁成本给穷人', color: '#BE185D' },
    ],
    speeches: [
      { id: 14, participantId: 11, content: '全面禁售燃油车,是环保刚需还是操之过急?请各位直接给判断。', reactionType: '开场', targetSpeechId: null, seq: 1, createdAt: '2026-07-03 09:30:05' },
      { id: 15, participantId: 12, content: '交通是城市碳排大头,越晚禁售,气候账单越贵,方向上没得商量。', reactionType: '举手', targetSpeechId: null, seq: 2, createdAt: '2026-07-03 09:30:23' },
      { id: 16, participantId: 14, content: '方向我同意,但现有电网在高峰期扛不住大规模充电,基建不先行就是空谈。', reactionType: '补充', targetSpeechId: null, seq: 3, createdAt: '2026-07-03 09:30:41' },
      { id: 17, participantId: 15, content: '而且一刀切禁售会让买不起新能源车的人承担转型成本,这不公平。', reactionType: '反驳', targetSpeechId: 15, seq: 4, createdAt: '2026-07-03 09:30:59' },
      { id: 18, participantId: 11, content: '所以大家不反对方向,分歧在节奏和配套。收在这里。', reactionType: '收尾', targetSpeechId: null, seq: 5, createdAt: '2026-07-03 09:31:16' },
    ],
    insights: [
      { id: 5, type: 'consensus', content: '减少燃油车是正确方向,但电网与充电基建是硬前置约束。', createdAt: '2026-07-03 09:31:02' },
      { id: 6, type: 'divergence', content: '应设硬性禁售时间表,还是靠市场与配套自然淘汰。', createdAt: '2026-07-03 09:31:03' },
    ],
  },
  {
    discussion: {
      id: 4, topic: '短视频对青少年是利大于弊还是弊大于利?', status: 'finished', expertCount: 4,
      createdAt: '2026-07-04 16:00:00',
      summary:
        '各方一致认为问题不在"短视频"本身而在"推荐算法的成瘾设计"与"使用时长失控"。分歧在于治理责任主要落在平台、家庭还是学校。共识落点:平台需承担算法透明与防沉迷的首要责任,家庭与教育做补位。',
    },
    participants: [
      { id: 16, role: 'host', name: '田甜', profession: '教育媒体人', title: '圆桌主持', stance: '中立引导', color: '#6B7280' },
      { id: 17, role: 'expert', name: '钱进', profession: '青少年心理学家', title: '主任医师', stance: '成瘾风险被低估', color: '#9333EA' },
      { id: 18, role: 'expert', name: '卢星', profession: '内容平台产品总监', title: '产品VP', stance: '工具中性看使用', color: '#0EA5E9' },
      { id: 19, role: 'expert', name: '邓宁', profession: '中学教师', title: '班主任', stance: '注意力碎片化严重', color: '#DC2626' },
      { id: 20, role: 'expert', name: '韩雪', profession: '媒介素养研究者', title: '副教授', stance: '关键在教会使用', color: '#16A34A' },
    ],
    speeches: [
      { id: 19, participantId: 16, content: '短视频对青少年,到底是知识普惠还是注意力鸦片?请各位亮牌。', reactionType: '开场', targetSpeechId: null, seq: 1, createdAt: '2026-07-04 16:00:05' },
      { id: 20, participantId: 17, content: '真正危险的不是内容,是无限下滑的推荐机制,它是按成瘾原理设计的。', reactionType: '举手', targetSpeechId: null, seq: 2, createdAt: '2026-07-04 16:00:24' },
      { id: 21, participantId: 18, content: '算法能成瘾,也能高效推知识,工具本身中性,问题在使用方式。', reactionType: '反驳', targetSpeechId: 20, seq: 3, createdAt: '2026-07-04 16:00:42' },
      { id: 22, participantId: 20, content: '所以出路是媒介素养教育,教会孩子主动使用而不是被动刷。', reactionType: '补充', targetSpeechId: null, seq: 4, createdAt: '2026-07-04 16:01:00' },
      { id: 23, participantId: 16, content: '共识是骂算法不骂视频,分歧在谁来管。收在这。', reactionType: '收尾', targetSpeechId: null, seq: 5, createdAt: '2026-07-04 16:01:18' },
    ],
    insights: [
      { id: 7, type: 'consensus', content: '核心问题在推荐算法的成瘾设计与时长失控,而非短视频本身。', createdAt: '2026-07-04 16:01:03' },
      { id: 8, type: 'divergence', content: '治理责任主要应落在平台、家庭还是学校。', createdAt: '2026-07-04 16:01:04' },
    ],
  },
  {
    discussion: {
      id: 5, topic: '通用人工智能(AGI)会在十年内到来吗?', status: 'finished', expertCount: 4,
      createdAt: '2026-07-05 11:00:00',
      summary:
        '讨论没有对"十年"达成一致,但共识是当前范式在推理与持续学习上仍有根本缺口,单靠扩大规模不必然通向 AGI。分歧在于缺口是工程问题还是需要范式突破。理性落点:与其押注时间表,不如关注能力的可验证里程碑。',
    },
    participants: [
      { id: 21, role: 'host', name: '罗盘', profession: '科技评论人', title: '圆桌主持', stance: '中立引导', color: '#6B7280' },
      { id: 22, role: 'expert', name: '叶航', profession: 'AI 研究科学家', title: '研究总监', stance: '规模化将带来突破', color: '#2563EB' },
      { id: 23, role: 'expert', name: '秦岭', profession: '认知科学家', title: '教授', stance: '缺持续学习能力', color: '#DB2777' },
      { id: 24, role: 'expert', name: '龚宇', profession: 'AI 安全学者', title: '副教授', stance: '别用时间表误导', color: '#D97706' },
      { id: 25, role: 'expert', name: '简凡', profession: '机器学习工程师', title: '资深工程师', stance: '工程缺口可补齐', color: '#0D9488' },
    ],
    speeches: [
      { id: 24, participantId: 21, content: 'AGI 十年内到来——是可预期的拐点还是又一次乐观泡沫?请各位判断。', reactionType: '开场', targetSpeechId: null, seq: 1, createdAt: '2026-07-05 11:00:05' },
      { id: 25, participantId: 22, content: '过去几年每次扩大规模都解锁了没预料到的能力,外推下去十年很可能到。', reactionType: '举手', targetSpeechId: null, seq: 2, createdAt: '2026-07-05 11:00:24' },
      { id: 26, participantId: 23, content: '但再大的模型也不会像人一样持续学习和形成因果理解,这是范式缺口。', reactionType: '反驳', targetSpeechId: 25, seq: 3, createdAt: '2026-07-05 11:00:43' },
      { id: 27, participantId: 24, content: '而且给出确定时间表本身就是误导,它会扭曲政策和投资判断。', reactionType: '补充', targetSpeechId: null, seq: 4, createdAt: '2026-07-05 11:01:01' },
      { id: 28, participantId: 21, content: '与其赌年份,不如盯可验证的能力里程碑。今天收在这。', reactionType: '收尾', targetSpeechId: null, seq: 5, createdAt: '2026-07-05 11:01:19' },
    ],
    insights: [
      { id: 9, type: 'consensus', content: '当前范式在推理与持续学习上存在根本缺口,单靠扩规模不必然通向 AGI。', createdAt: '2026-07-05 11:01:05' },
      { id: 10, type: 'divergence', content: 'AGI 的缺口是可补齐的工程问题,还是需要范式级突破。', createdAt: '2026-07-05 11:01:06' },
    ],
  },
]
