package com.panel.service;

import com.panel.ai.dto.InsightDraft;
import com.panel.ai.dto.TurnProposal;
import com.panel.entity.Insight;
import com.panel.mapper.InsightMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

// C. 共识/分歧提炼:仅主持人回合产出;解析/去重/入库;实时
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InsightExtractorTest {

    @Mock
    InsightMapper insightMapper;

    InsightExtractor ext() {
        return new InsightExtractor(insightMapper);
    }

    private TurnProposal hostTurn(List<InsightDraft> insights) {
        return new TurnProposal(1L, "串联", null, "串联一下。", null, insights);
    }

    @Test
    void hostTurn_producesInsights() {
        when(insightMapper.selectByDiscussion(1L)).thenReturn(new ArrayList<>());
        ext().extract(hostTurn(List.of(
                new InsightDraft("consensus", "共识A"),
                new InsightDraft("divergence", "分歧B"))), true, 1L);
        verify(insightMapper, times(2)).insert(any(Insight.class));
    }

    @Test
    void expertTurn_producesNoInsight() {
        TurnProposal expert = new TurnProposal(2L, "举手", null, "观点。", null,
                List.of(new InsightDraft("consensus", "不该产出")));
        ext().extract(expert, false, 1L);
        verify(insightMapper, never()).insert(any(Insight.class));
    }

    @Test
    void duplicateInsight_deduped() {
        Insight existing = new Insight();
        existing.setType("consensus");
        existing.setContent("共识A");
        when(insightMapper.selectByDiscussion(1L)).thenReturn(List.of(existing));
        ext().extract(hostTurn(List.of(
                new InsightDraft("consensus", "共识A"),   // 重复,应跳过
                new InsightDraft("consensus", "共识C"))), true, 1L);
        verify(insightMapper, times(1)).insert(any(Insight.class));
    }

    @Test
    void insight_parsedIntoTypeAndContent() {
        when(insightMapper.selectByDiscussion(1L)).thenReturn(new ArrayList<>());
        ext().extract(hostTurn(List.of(new InsightDraft("consensus", "共识内容"))), true, 1L);
        ArgumentCaptor<Insight> cap = ArgumentCaptor.forClass(Insight.class);
        verify(insightMapper).insert(cap.capture());
        Insight got = cap.getValue();
        assertEquals("consensus", got.getType());
        assertEquals("共识内容", got.getContent());
        assertEquals(1L, got.getDiscussionId());
    }

    @Test
    void insight_persistedRealtime_notDeferred() {
        // 讨论进行中的主持人回合即刻入库(非等收尾)
        when(insightMapper.selectByDiscussion(1L)).thenReturn(new ArrayList<>());
        ext().extract(hostTurn(List.of(new InsightDraft("divergence", "实时分歧"))), true, 1L);
        verify(insightMapper, atLeastOnce()).insert(any(Insight.class));
    }

    @Test
    void malformedInsightEntry_skipped() {
        when(insightMapper.selectByDiscussion(anyLong())).thenReturn(new ArrayList<>());
        assertDoesNotThrow(() -> ext().extract(hostTurn(List.of(
                new InsightDraft(null, "缺 type"),
                new InsightDraft("consensus", "有效"))), true, 1L));
        verify(insightMapper, times(1)).insert(any(Insight.class));
    }
}
