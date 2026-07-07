package com.panel.service;

import com.panel.ai.dto.TurnProposal;
import com.panel.mapper.InsightMapper;
import org.springframework.stereotype.Service;

// 共识/分歧提炼:仅主持人回合产出;解析/去重/入库。RED 阶段桩:未实现。
@Service
public class InsightExtractor {

    private final InsightMapper insightMapper;

    public InsightExtractor(InsightMapper insightMapper) {
        this.insightMapper = insightMapper;
    }

    // isHostTurn=false 时不产出任何 insight;去重按 (type,content)
    public void extract(TurnProposal proposal, boolean isHostTurn, long discussionId) {
        throw new UnsupportedOperationException("RED: not implemented");
    }
}
