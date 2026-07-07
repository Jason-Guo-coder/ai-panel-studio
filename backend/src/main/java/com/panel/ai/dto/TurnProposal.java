package com.panel.ai.dto;

import java.util.List;

// P2 每轮发言提案(尚未落库):谁说、反应类型、反驳目标、内容、公开关注点、共识/分歧
public record TurnProposal(
        Long speakerId,
        String reactionType,
        Long targetSpeechId,
        String content,
        String focus,
        List<InsightDraft> insights) {
}
