package com.panel.engine;

import com.panel.ai.AiService;
import com.panel.ai.dto.TurnContext;
import com.panel.ai.dto.TurnProposal;
import com.panel.entity.Participant;
import com.panel.entity.Speech;
import com.panel.mapper.DiscussionMapper;
import com.panel.mapper.SpeechMapper;
import com.panel.service.InsightExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// B(编排):失败降级 + 硬上限收尾。AI 与 Mapper 全 mock,零网络。
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DiscussionEngineTest {

    @Mock AiService ai;
    @Mock InsightExtractor insightExtractor;
    @Mock DiscussionMapper discussionMapper;
    @Mock SpeechMapper speechMapper;
    @Mock EventPublisher events;

    DiscussionEngine engine() {
        return new DiscussionEngine(ai, new TurnScheduler(), insightExtractor, discussionMapper, speechMapper, events);
    }

    private Participant p(long id, String role) {
        Participant p = new Participant();
        p.setId(id);
        p.setRole(role);
        p.setName("n" + id);
        return p;
    }

    private List<Participant> roster() {
        return List.of(p(1, "host"), p(2, "expert"), p(3, "expert"), p(4, "expert"));
    }

    private TurnContext ctx() {
        List<Speech> t = new ArrayList<>();
        Speech s = new Speech();
        s.setId(1L); s.setParticipantId(1L); s.setSeq(1); s.setReactionType("开场"); s.setContent("开场。");
        t.add(s);
        return new TurnContext(1L, t, roster(), 1, 0);
    }

    private TurnProposal validExpertTurn() {
        return new TurnProposal(2L, "举手", null, "一个观点。", "关注点", List.of());
    }

    private TurnProposal invalidRebuttal() {
        return new TurnProposal(3L, "反驳", null, "反驳。", null, List.of()); // 反驳缺 target → 非法
    }

    @Test
    void p2Fails_retriesOnce_thenSucceeds() {
        when(ai.proposeTurn(any())).thenThrow(new RuntimeException("boom")).thenReturn(validExpertTurn());
        TurnProposal r = engine().produceValidatedTurn(ctx());
        assertNotNull(r);
        verify(ai, times(2)).proposeTurn(any());
    }

    @Test
    void p2FailsTwice_forcesHostTurn() {
        when(ai.proposeTurn(any())).thenReturn(invalidRebuttal(), invalidRebuttal());
        TurnProposal r = engine().produceValidatedTurn(ctx());
        assertEquals(1L, r.speakerId(), "连续非法后应强制主持人回合");
    }

    @Test
    void persistentFailure_pushesErrorAndPauses() {
        when(ai.proposeTurn(any())).thenThrow(new RuntimeException("always"));
        engine().runDiscussion(1L, roster());
        verify(events).error(eq(1L), anyString());
        verify(discussionMapper).updateStatus(eq(1L), eq("interrupted"));
    }

    @Test
    void hardCap16_forcesHostClosingAndFinish() {
        when(ai.proposeTurn(any())).thenReturn(validExpertTurn());
        when(ai.summarize(any())).thenReturn("这是主持人的自然语言总结。");
        engine().runDiscussion(1L, roster());
        verify(speechMapper, atLeastOnce()).insert(argThat((Speech s) -> "收尾".equals(s.getReactionType())));
        verify(discussionMapper).updateStatus(eq(1L), eq("finished"));
        verify(discussionMapper).updateSummary(eq(1L), argThat(s -> s != null && !s.isBlank()));
    }
}
