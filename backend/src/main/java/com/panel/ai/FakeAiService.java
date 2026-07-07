package com.panel.ai;

import com.panel.ai.dto.*;
import com.panel.entity.Participant;
import com.panel.entity.Speech;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// 确定性"假 AI":集成测试 / E2E 用,零网络、零花费、可重复。产出合法的 P1/P2/P3。
@Profile("fake-ai")
@Service
public class FakeAiService implements AiService {

    @Override
    public RosterDraft draftRoster(String topic, int expertCount) {
        // E2E 确定性失败钩子:话题含 __FAIL__ 触发上游异常,用于验证前端错误态渲染
        if (topic != null && topic.contains("__FAIL__")) {
            throw new AiUpstreamException("E2E 触发的阵容生成失败");
        }
        DraftMember host = new DraftMember("主持人阿岚", "圆桌主持人", "主持", "中立引导");
        List<DraftMember> experts = new ArrayList<>();
        for (int i = 1; i <= expertCount; i++) {
            experts.add(new DraftMember("专家" + i, "职业" + i, "Title" + i, "立场观点" + i));
        }
        return new RosterDraft(host, experts);
    }

    @Override
    public TurnProposal proposeTurn(TurnContext ctx) {
        List<Participant> roster = ctx.roster();
        long hostId = roster.stream().filter(p -> "host".equals(p.getRole()))
                .map(Participant::getId).findFirst().orElseThrow();
        List<Participant> experts = roster.stream().filter(p -> "expert".equals(p.getRole())).toList();
        List<Speech> t = ctx.transcript();

        if (t.isEmpty()) {
            return new TurnProposal(hostId, "开场", null, "欢迎各位,我们开始今天的圆桌讨论。", null, List.of());
        }
        Speech last = t.get(t.size() - 1);

        // 到点由主持人串联,并产出共识/分歧(仅主持人回合)
        if (ctx.forceHost()) {
            return new TurnProposal(hostId, "串联", null, "我先把大家的观点串一下。", null,
                    List.of(new InsightDraft("consensus", "各方对核心事实达成共识。"),
                            new InsightDraft("divergence", "在落地路径上仍存在分歧。")));
        }

        // 选一个非上一发言人的专家
        Participant speaker = experts.stream()
                .filter(e -> !e.getId().equals(last.getParticipantId()))
                .findFirst().orElse(experts.get(0));

        if (t.size() % 4 == 0) {
            return new TurnProposal(speaker.getId(), "反驳", last.getId(), "我对上一点有不同看法。",
                    speaker.getStance(), List.of());
        }
        return new TurnProposal(speaker.getId(), "举手", null, "这是我的一个观点。",
                speaker.getStance(), List.of());
    }

    @Override
    public String summarize(TurnContext ctx) {
        return "综合来看,各方在核心问题上形成了基本共识,但在具体落地路径上仍有分歧,值得继续探讨。";
    }
}
