package com.panel.engine;

import com.panel.ai.dto.TurnProposal;
import com.panel.entity.Participant;
import com.panel.entity.Speech;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// 发言调度硬规则(纯函数,无 AI 依赖)。
@Component
public class TurnScheduler {

    private static final int MAX_SENTENCES = 2;
    private static final String OPENING = "开场";
    private static final String SUPPLEMENT = "补充";
    private static final String REBUTTAL = "反驳";

    // 校验一条 P2 提案是否合法
    public ValidationResult validate(TurnProposal proposal, List<Speech> transcript, List<Participant> roster) {
        Set<Long> memberIds = roster.stream().map(Participant::getId).collect(Collectors.toSet());
        if (proposal.speakerId() == null || !memberIds.contains(proposal.speakerId())) {
            return ValidationResult.invalid("speakerId 不在阵容中");
        }
        if (proposal.content() == null || proposal.content().isBlank()) {
            return ValidationResult.invalid("发言内容为空");
        }

        // 开局:必须主持人开场
        if (transcript.isEmpty()) {
            boolean hostOpening = isHost(proposal.speakerId(), roster) && OPENING.equals(proposal.reactionType());
            return hostOpening ? ValidationResult.ok() : ValidationResult.invalid("开局须由主持人开场");
        }

        // 反驳:必须指向已存在的发言
        if (REBUTTAL.equals(proposal.reactionType())) {
            Set<Long> speechIds = transcript.stream().map(Speech::getId).collect(Collectors.toSet());
            if (proposal.targetSpeechId() == null || !speechIds.contains(proposal.targetSpeechId())) {
                return ValidationResult.invalid("反驳须指向已存在的发言");
            }
        }

        // 不许连说:唯一例外是"补充自己上一句"
        Speech last = transcript.get(transcript.size() - 1);
        if (proposal.speakerId().equals(last.getParticipantId())) {
            boolean supplementOwnLast = SUPPLEMENT.equals(proposal.reactionType())
                    && last.getId() != null && last.getId().equals(proposal.targetSpeechId());
            if (!supplementOwnLast) {
                return ValidationResult.invalid("不许连说(补充自己上一句除外)");
            }
        }

        return ValidationResult.ok();
    }

    // 软校验:发言超 2 句截断至 ≤2 句
    public String enforceSentenceLimit(String content) {
        if (content == null) {
            return null;
        }
        int enders = 0;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '。' || c == '!' || c == '?' || c == '！' || c == '？' || c == '.') {
                enders++;
                if (enders == MAX_SENTENCES) {
                    return content.substring(0, i + 1);
                }
            }
        }
        return content;
    }

    // 主持人节奏:累计专家发言达 3–4 条应插入主持人回合
    public boolean shouldHostInterject(int expertTurnsSinceLastHost) {
        return expertTurnsSinceLastHost >= 3;
    }

    private boolean isHost(Long id, List<Participant> roster) {
        return roster.stream().anyMatch(p -> p.getId().equals(id) && "host".equals(p.getRole()));
    }
}
