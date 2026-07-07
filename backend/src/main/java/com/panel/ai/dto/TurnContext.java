package com.panel.ai.dto;

import com.panel.entity.Participant;
import com.panel.entity.Speech;

import java.util.List;

// 一轮调度所需的上下文。forceHost=true 表示按节奏必须由主持人以"串联"发言并提炼共识/分歧。
public record TurnContext(
        long discussionId,
        List<Speech> transcript,
        List<Participant> roster,
        int speechCount,
        int expertTurnsSinceLastHost,
        boolean forceHost) {
}
