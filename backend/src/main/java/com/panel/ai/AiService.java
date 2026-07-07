package com.panel.ai;

import com.panel.ai.dto.RosterDraft;
import com.panel.ai.dto.TurnContext;
import com.panel.ai.dto.TurnProposal;

// AI 编排三调用 P1/P2/P3 的类型化边界;测试用 Mockito 打桩,零网络
public interface AiService {
    RosterDraft draftRoster(String topic, int expertCount);   // P1

    TurnProposal proposeTurn(TurnContext ctx);                // P2

    String summarize(TurnContext ctx);                        // P3
}
