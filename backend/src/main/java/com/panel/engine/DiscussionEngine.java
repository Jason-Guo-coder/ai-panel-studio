package com.panel.engine;

import com.panel.ai.AiService;
import com.panel.ai.dto.TurnContext;
import com.panel.ai.dto.TurnProposal;
import com.panel.entity.Participant;
import com.panel.mapper.DiscussionMapper;
import com.panel.mapper.SpeechMapper;
import com.panel.service.InsightExtractor;

import java.util.List;

// 讨论推进编排(循环 + 失败降级 + 硬上限收尾)。RED 阶段桩:未实现。
public class DiscussionEngine {

    private final AiService ai;
    private final TurnScheduler scheduler;
    private final InsightExtractor insightExtractor;
    private final DiscussionMapper discussionMapper;
    private final SpeechMapper speechMapper;
    private final EventPublisher events;

    public DiscussionEngine(AiService ai, TurnScheduler scheduler, InsightExtractor insightExtractor,
                            DiscussionMapper discussionMapper, SpeechMapper speechMapper, EventPublisher events) {
        this.ai = ai;
        this.scheduler = scheduler;
        this.insightExtractor = insightExtractor;
        this.discussionMapper = discussionMapper;
        this.speechMapper = speechMapper;
        this.events = events;
    }

    // 产出一条合法提案:非法/抛错重试 1 次 → 仍失败强制主持人回合 → 连崩上抛暂停
    public TurnProposal produceValidatedTurn(TurnContext ctx) {
        throw new UnsupportedOperationException("RED: not implemented");
    }

    // 跑完整讨论循环:主持人开场/节奏插入/硬上限 16 收尾;失败降级到 error+暂停
    public void runDiscussion(long discussionId, List<Participant> roster) {
        throw new UnsupportedOperationException("RED: not implemented");
    }
}
