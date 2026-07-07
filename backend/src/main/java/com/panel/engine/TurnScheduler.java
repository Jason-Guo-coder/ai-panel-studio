package com.panel.engine;

import com.panel.ai.dto.TurnProposal;
import com.panel.entity.Participant;
import com.panel.entity.Speech;
import org.springframework.stereotype.Component;

import java.util.List;

// 发言调度硬规则(纯函数,无 AI 依赖)。RED 阶段桩:未实现。
@Component
public class TurnScheduler {

    // 校验一条 P2 提案是否合法(speaker 存在/反驳有效 target/不许连说·补充自己例外/开局须主持人开场/内容非空)
    public ValidationResult validate(TurnProposal proposal, List<Speech> transcript, List<Participant> roster) {
        throw new UnsupportedOperationException("RED: not implemented");
    }

    // 软校验:发言超 2 句截断至 ≤2 句
    public String enforceSentenceLimit(String content) {
        throw new UnsupportedOperationException("RED: not implemented");
    }

    // 主持人节奏:累计专家发言达 3–4 条应插入主持人回合
    public boolean shouldHostInterject(int expertTurnsSinceLastHost) {
        throw new UnsupportedOperationException("RED: not implemented");
    }
}
