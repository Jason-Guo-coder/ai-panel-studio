package com.panel.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.panel.ai.dto.*;
import com.panel.entity.Participant;
import com.panel.entity.Speech;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// AiService 的真实实现:P1/P2/P3 prompt 模板 + JSON 解析。解析失败一律抛 AiUpstreamException。
// fake-ai profile 下停用,让确定性的 FakeAiService 顶上(E2E/集成零花费)。
@Service
@Profile("!fake-ai")
public class DeepseekAiService implements AiService {

    private static final String P1_SYSTEM = """
            你是圆桌讨论的选角导演。根据话题与专家人数,设计 1 位主持人与 N 位专家。
            只输出 JSON,禁止多余文字或代码块围栏。格式:
            {"host":{"name","profession","title","stance"},
             "experts":[{"name","profession","title","stance"}]}
            要求:专家立场彼此有区分;不要输出颜色字段(颜色由系统指派)。""";

    private static final String P2_SYSTEM = """
            你是圆桌讨论引擎。依据当前 transcript 内容,决定"谁最有话接"下一句(非机械轮流),
            输出一条 1–2 句发言。只输出 JSON,禁止围栏。格式:
            {"speakerId":<number>,"reactionType":"开场|串联|追问|收尾|举手|抢答|补充|反驳",
             "targetSpeechId":<number|null>,"content":"1-2句","focus":"公开关注点|null",
             "insights":[{"type":"consensus|divergence","content":"..."}]}
            规则:反驳必须给出已存在发言的 targetSpeechId;同一人不得连说(补充自己上一句除外);
            共识/分歧仅在主持人(串联)回合填 insights,专家回合 insights 留空数组。""";

    private static final String P3_SYSTEM = """
            你是圆桌主持人。用自然语言对整场讨论做简明总结(不要输出 JSON、不要罗列条目),
            突出达成的共识与仍存的分歧。""";

    private final DeepseekClient client;
    private final ObjectMapper om = new ObjectMapper();

    public DeepseekAiService(DeepseekClient client) {
        this.client = client;
    }

    @Override
    public RosterDraft draftRoster(String topic, int expertCount) {
        String user = "话题:" + topic + "\n专家人数:" + expertCount;
        JsonNode root = parse(client.chat(P1_SYSTEM, user));
        try {
            DraftMember host = member(root.get("host"));
            List<DraftMember> experts = new ArrayList<>();
            for (JsonNode e : root.get("experts")) {
                experts.add(member(e));
            }
            return new RosterDraft(host, experts);
        } catch (Exception e) {
            throw new AiUpstreamException("P1 阵容解析失败:" + e.getMessage());
        }
    }

    @Override
    public TurnProposal proposeTurn(TurnContext ctx) {
        JsonNode root = parse(client.chat(P2_SYSTEM, renderContext(ctx)));
        try {
            List<InsightDraft> insights = new ArrayList<>();
            if (root.has("insights") && root.get("insights").isArray()) {
                for (JsonNode n : root.get("insights")) {
                    insights.add(new InsightDraft(text(n, "type"), text(n, "content")));
                }
            }
            return new TurnProposal(
                    root.path("speakerId").asLong(),
                    text(root, "reactionType"),
                    root.hasNonNull("targetSpeechId") ? root.get("targetSpeechId").asLong() : null,
                    text(root, "content"),
                    text(root, "focus"),
                    insights);
        } catch (Exception e) {
            throw new AiUpstreamException("P2 发言解析失败:" + e.getMessage());
        }
    }

    @Override
    public String summarize(TurnContext ctx) {
        return client.chat(P3_SYSTEM, renderContext(ctx));
    }

    // ── 内部 ──

    private DraftMember member(JsonNode n) {
        return new DraftMember(text(n, "name"), text(n, "profession"), text(n, "title"), text(n, "stance"));
    }

    private String text(JsonNode n, String field) {
        JsonNode v = n.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }

    // 去掉可能的 ```json 围栏后解析
    private JsonNode parse(String raw) {
        try {
            String s = raw == null ? "" : raw.trim();
            if (s.startsWith("```")) {
                s = s.replaceAll("^```[a-zA-Z]*", "").replaceAll("```$", "").trim();
            }
            return om.readTree(s);
        } catch (Exception e) {
            throw new AiUpstreamException("模型输出非合法 JSON:" + e.getMessage());
        }
    }

    private String renderContext(TurnContext ctx) {
        String roster = ctx.roster().stream()
                .map(p -> p.getId() + ":" + p.getRole() + " " + p.getName() + "(" + p.getStance() + ")")
                .collect(Collectors.joining("\n"));
        String transcript = ctx.transcript().stream()
                .map(s -> "#" + s.getId() + " p" + s.getParticipantId() + ": " + s.getContent())
                .collect(Collectors.joining("\n"));
        return "阵容:\n" + roster + "\n\n当前记录:\n" + (transcript.isEmpty() ? "(空,待开场)" : transcript);
    }
}
