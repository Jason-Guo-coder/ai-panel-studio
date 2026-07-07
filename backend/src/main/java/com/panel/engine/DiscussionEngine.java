package com.panel.engine;

import com.panel.ai.AiService;
import com.panel.ai.dto.TurnContext;
import com.panel.ai.dto.TurnProposal;
import com.panel.entity.Participant;
import com.panel.entity.Speech;
import com.panel.mapper.DiscussionMapper;
import com.panel.mapper.SpeechMapper;
import com.panel.service.InsightExtractor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// 讨论推进编排:循环 + 失败降级 + 硬上限收尾。
public class DiscussionEngine {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int DEFAULT_MAX_SPEECHES = 16;
    private static final int MAX_CONSECUTIVE_DEGRADED = 2; // 连续降级到此即判 AI 持续失败 → 暂停

    private final AiService ai;
    private final TurnScheduler scheduler;
    private final InsightExtractor insightExtractor;
    private final DiscussionMapper discussionMapper;
    private final SpeechMapper speechMapper;
    private final EventPublisher events;
    private final int maxSpeeches;

    public DiscussionEngine(AiService ai, TurnScheduler scheduler, InsightExtractor insightExtractor,
                            DiscussionMapper discussionMapper, SpeechMapper speechMapper, EventPublisher events) {
        this(ai, scheduler, insightExtractor, discussionMapper, speechMapper, events, DEFAULT_MAX_SPEECHES);
    }

    public DiscussionEngine(AiService ai, TurnScheduler scheduler, InsightExtractor insightExtractor,
                            DiscussionMapper discussionMapper, SpeechMapper speechMapper, EventPublisher events,
                            int maxSpeeches) {
        this.ai = ai;
        this.scheduler = scheduler;
        this.insightExtractor = insightExtractor;
        this.discussionMapper = discussionMapper;
        this.speechMapper = speechMapper;
        this.events = events;
        this.maxSpeeches = maxSpeeches;
    }

    // 产出一条合法提案:非法/抛错重试 1 次 → 仍失败强制主持人回合(不抛出)
    public TurnProposal produceValidatedTurn(TurnContext ctx) {
        return attempt(ctx).proposal;
    }

    // 跑完整讨论循环
    public void runDiscussion(long discussionId, List<Participant> roster) {
        List<Speech> transcript = new ArrayList<>();
        int expertTurnsSinceHost = 0;
        int consecutiveDegraded = 0;

        while (transcript.size() < maxSpeeches) {
            TurnContext ctx = new TurnContext(discussionId, transcript, roster, transcript.size(), expertTurnsSinceHost);

            // 主持人节奏:累计专家发言达阈值,强制插入一个主持人回合(非降级)
            TurnProposal turn;
            boolean degraded;
            if (!transcript.isEmpty() && scheduler.shouldHostInterject(expertTurnsSinceHost)) {
                turn = forcedHostTurn(ctx);
                degraded = false;
            } else {
                Attempt a = attempt(ctx);
                turn = a.proposal;
                degraded = a.degraded;
            }

            if (degraded) {
                consecutiveDegraded++;
                if (consecutiveDegraded >= MAX_CONSECUTIVE_DEGRADED) {
                    // AI 连续失败,连主持人回合都拿不到有效输出 → 暂停
                    events.error(discussionId, "AI 调用连续失败,讨论已暂停,请稍后重试");
                    discussionMapper.updateStatus(discussionId, "interrupted");
                    return;
                }
            } else {
                consecutiveDegraded = 0;
            }

            Speech saved = persist(discussionId, turn, transcript.size() + 1);
            transcript.add(saved);
            events.speech(discussionId, saved);

            boolean isHostTurn = isHost(turn.speakerId(), roster);
            insightExtractor.extract(turn, isHostTurn, discussionId);
            expertTurnsSinceHost = isHostTurn ? 0 : expertTurnsSinceHost + 1;
        }

        // 硬上限达成 → 主持人收尾
        close(discussionId, roster, transcript);
    }

    // ── 内部 ──

    private record Attempt(TurnProposal proposal, boolean degraded) {
    }

    // 重试 1 次(共 2 次尝试);仍失败 → 合成主持人回合(约束最松,几乎必合法),degraded=true
    private Attempt attempt(TurnContext ctx) {
        for (int i = 0; i < 2; i++) {
            try {
                TurnProposal p = ai.proposeTurn(ctx);
                if (scheduler.validate(p, ctx.transcript(), ctx.roster()).valid()) {
                    return new Attempt(p, false);
                }
            } catch (RuntimeException e) {
                // 吞掉,进入重试/降级
            }
        }
        return new Attempt(forcedHostTurn(ctx), true);
    }

    private TurnProposal forcedHostTurn(TurnContext ctx) {
        long hostId = ctx.roster().stream()
                .filter(p -> "host".equals(p.getRole()))
                .map(Participant::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("阵容缺少主持人"));
        boolean opening = ctx.transcript().isEmpty();
        String type = opening ? "开场" : "串联";
        String content = opening ? "各位好,我们开始今天的圆桌讨论。" : "我们把目前的观点串一下。";
        return new TurnProposal(hostId, type, null, content, null, List.of());
    }

    private Speech persist(long discussionId, TurnProposal turn, int seq) {
        Speech s = new Speech();
        s.setId((long) seq); // 内存 transcript 用 seq 作 id;真实库由 AUTO 回填
        s.setDiscussionId(discussionId);
        s.setParticipantId(turn.speakerId());
        s.setContent(scheduler.enforceSentenceLimit(turn.content()));
        s.setReactionType(turn.reactionType());
        s.setTargetSpeechId(turn.targetSpeechId());
        s.setSeq(seq);
        s.setCreatedAt(LocalDateTime.now().format(TS));
        speechMapper.insert(s);
        return s;
    }

    private void close(long discussionId, List<Participant> roster, List<Speech> transcript) {
        TurnContext ctx = new TurnContext(discussionId, transcript, roster, transcript.size(), 0);
        String summary = ai.summarize(ctx);

        long hostId = roster.stream().filter(p -> "host".equals(p.getRole()))
                .map(Participant::getId).findFirst().orElseThrow();
        Speech closing = new Speech();
        closing.setDiscussionId(discussionId);
        closing.setParticipantId(hostId);
        closing.setContent("感谢各位,今天的讨论到此。");
        closing.setReactionType("收尾");
        closing.setSeq(transcript.size() + 1);
        closing.setCreatedAt(LocalDateTime.now().format(TS));
        speechMapper.insert(closing);

        discussionMapper.updateSummary(discussionId, summary);
        discussionMapper.updateStatus(discussionId, "finished");
        events.summary(discussionId, summary);
        events.finished(discussionId);
    }

    private boolean isHost(Long id, List<Participant> roster) {
        return roster.stream().anyMatch(p -> p.getId().equals(id) && "host".equals(p.getRole()));
    }
}
