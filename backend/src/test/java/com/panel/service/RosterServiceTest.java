package com.panel.service;

import com.panel.ai.AiService;
import com.panel.ai.AiUpstreamException;
import com.panel.ai.dto.DraftMember;
import com.panel.ai.dto.RosterDraft;
import com.panel.engine.ValidationException;
import com.panel.entity.Participant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

// A. 嘉宾生成(P1)—— 颜色一律后端按调色板指派、不信 LLM 配色(P1 可不出颜色)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RosterServiceTest {

    @Mock
    AiService ai;

    RosterService svc() {
        return new RosterService(ai);
    }

    private RosterDraft draft(int n) {
        DraftMember host = new DraftMember("林知远", "科技媒体人", "圆桌主持", "中立引导");
        List<DraftMember> experts = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            experts.add(new DraftMember("专家" + i, "职业" + i, "Title" + i, "立场" + i));
        }
        return new RosterDraft(host, experts);
    }

    private List<Participant> experts(List<Participant> all) {
        return all.stream().filter(p -> "expert".equals(p.getRole())).collect(Collectors.toList());
    }

    private List<Participant> hosts(List<Participant> all) {
        return all.stream().filter(p -> "host".equals(p.getRole())).collect(Collectors.toList());
    }

    @Test
    void generateRoster_returnsOneHostAndNExperts() {
        when(ai.draftRoster("话题", 4)).thenReturn(draft(4));
        List<Participant> r = svc().generateRoster("话题", 4);
        assertEquals(1, hosts(r).size());
        assertEquals(4, experts(r).size());
    }

    @Test
    void expertColors_allDistinct_viaPalette() {
        when(ai.draftRoster("话题", 4)).thenReturn(draft(4));
        List<Participant> r = svc().generateRoster("话题", 4);
        Set<String> colors = experts(r).stream().map(Participant::getColor).collect(Collectors.toSet());
        assertEquals(4, colors.size(), "专家颜色应互不相同");
    }

    @Test
    void expertStances_presentAndDiffer() {
        when(ai.draftRoster("话题", 4)).thenReturn(draft(4));
        List<Participant> r = svc().generateRoster("话题", 4);
        assertTrue(experts(r).stream().allMatch(p -> p.getStance() != null && !p.getStance().isBlank()));
        assertTrue(experts(r).stream().map(Participant::getStance).distinct().count() >= 2);
    }

    @Test
    void allFields_complete() {
        when(ai.draftRoster("话题", 4)).thenReturn(draft(4));
        List<Participant> r = svc().generateRoster("话题", 4);
        for (Participant p : r) {
            assertNotNull(p.getName());
            assertFalse(p.getName().isBlank());
            assertFalse(p.getProfession().isBlank());
            assertFalse(p.getTitle().isBlank());
            assertFalse(p.getStance().isBlank());
            assertFalse(p.getColor().isBlank());
        }
    }

    @Test
    void hostColor_isNeutralGray_notInExpertPalette() {
        when(ai.draftRoster("话题", 4)).thenReturn(draft(4));
        List<Participant> r = svc().generateRoster("话题", 4);
        Participant host = hosts(r).get(0);
        assertEquals("#6B7280", host.getColor());
        Set<String> expertColors = experts(r).stream().map(Participant::getColor).collect(Collectors.toSet());
        assertFalse(expertColors.contains(host.getColor()));
    }

    @Test
    void defaultExpertCount_isFour() {
        when(ai.draftRoster("话题", 4)).thenReturn(draft(4));
        List<Participant> r = svc().generateRoster("话题", null);
        assertEquals(4, experts(r).size());
    }

    @Test
    void expertCount_belowMin_rejected() {
        assertThrows(ValidationException.class, () -> svc().generateRoster("话题", 1));
    }

    @Test
    void expertCount_aboveMax_rejected() {
        assertThrows(ValidationException.class, () -> svc().generateRoster("话题", 7));
    }

    @Test
    void expertCount_boundsAccepted() {
        when(ai.draftRoster("话题", 2)).thenReturn(draft(2));
        when(ai.draftRoster("话题", 6)).thenReturn(draft(6));
        assertEquals(2, experts(svc().generateRoster("话题", 2)).size());
        assertEquals(6, experts(svc().generateRoster("话题", 6)).size());
    }

    @Test
    void malformedP1_surfacedAsUpstreamError() {
        when(ai.draftRoster(anyString(), anyInt())).thenThrow(new AiUpstreamException("bad json"));
        assertThrows(AiUpstreamException.class, () -> svc().generateRoster("话题", 4));
    }
}
