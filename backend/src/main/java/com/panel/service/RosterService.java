package com.panel.service;

import com.panel.ai.AiService;
import com.panel.ai.dto.DraftMember;
import com.panel.ai.dto.RosterDraft;
import com.panel.engine.ValidationException;
import com.panel.entity.Participant;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// 嘉宾生成(P1):校验人数 → 调 P1 取人设 → 后端按调色板指派颜色(不信 LLM 配色)→ 组装。
@Service
public class RosterService {

    private static final int DEFAULT_COUNT = 4;
    private static final int MIN = 2;
    private static final int MAX = 6;
    private static final String HOST_COLOR = "#6B7280"; // 主持人中性灰,不入专家色板
    private static final String[] PALETTE = {
            "#2563EB", "#DB2777", "#16A34A", "#F59E0B",
            "#7C3AED", "#0891B2", "#EA580C", "#0D9488"
    };

    private final AiService ai;

    public RosterService(AiService ai) {
        this.ai = ai;
    }

    public List<Participant> generateRoster(String topic, Integer expertCount) {
        int count = expertCount == null ? DEFAULT_COUNT : expertCount;
        if (count < MIN || count > MAX) {
            throw new ValidationException("专家人数需在 " + MIN + "–" + MAX + " 之间,收到:" + count);
        }
        RosterDraft draft = ai.draftRoster(topic, count); // 解析失败由 AiService 抛 AiUpstreamException

        List<Participant> roster = new ArrayList<>();
        roster.add(toParticipant(draft.host(), "host", HOST_COLOR));
        List<DraftMember> experts = draft.experts();
        for (int i = 0; i < experts.size(); i++) {
            roster.add(toParticipant(experts.get(i), "expert", PALETTE[i % PALETTE.length]));
        }
        return roster;
    }

    private Participant toParticipant(DraftMember m, String role, String color) {
        Participant p = new Participant();
        p.setRole(role);
        p.setName(m.name());
        p.setProfession(m.profession());
        p.setTitle(m.title());
        p.setStance(m.stance());
        p.setColor(color);
        return p;
    }
}
