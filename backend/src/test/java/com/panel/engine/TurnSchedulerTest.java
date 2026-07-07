package com.panel.engine;

import com.panel.ai.dto.TurnProposal;
import com.panel.entity.Participant;
import com.panel.entity.Speech;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// B. 发言调度硬规则(纯函数)
class TurnSchedulerTest {

    final TurnScheduler sched = new TurnScheduler();

    private Participant p(long id, String role) {
        Participant p = new Participant();
        p.setId(id);
        p.setRole(role);
        p.setName("n" + id);
        return p;
    }

    private Speech sp(long id, long pid, int seq, String type) {
        Speech s = new Speech();
        s.setId(id);
        s.setParticipantId(pid);
        s.setSeq(seq);
        s.setReactionType(type);
        s.setContent("句。");
        return s;
    }

    private TurnProposal prop(Long speaker, String type, Long target, String content) {
        return new TurnProposal(speaker, type, target, content, null, List.of());
    }

    // 阵容:host=1, experts=2,3,4
    private List<Participant> roster() {
        return List.of(p(1, "host"), p(2, "expert"), p(3, "expert"), p(4, "expert"));
    }

    // 进行中的 transcript:开场(host,speech1) + expert2 发言(speech2)
    private List<Speech> ongoing() {
        List<Speech> t = new ArrayList<>();
        t.add(sp(1, 1, 1, "开场"));
        t.add(sp(2, 2, 2, "举手"));
        return t;
    }

    @Test
    void nextSpeaker_isLlmChosen_notRoundRobin() {
        // 上一发言 expert2,提案跳到 expert4(非顺序轮流)→ 应接受
        ValidationResult r = sched.validate(prop(4L, "举手", null, "一句。"), ongoing(), roster());
        assertTrue(r.valid());
    }

    @Test
    void rebuttal_withValidTarget_accepted() {
        ValidationResult r = sched.validate(prop(3L, "反驳", 2L, "反驳。"), ongoing(), roster());
        assertTrue(r.valid());
    }

    @Test
    void rebuttal_withInvalidTarget_rejected() {
        ValidationResult r = sched.validate(prop(3L, "反驳", 999L, "反驳。"), ongoing(), roster());
        assertFalse(r.valid());
    }

    @Test
    void rebuttal_withNullTarget_rejected() {
        ValidationResult r = sched.validate(prop(3L, "反驳", null, "反驳。"), ongoing(), roster());
        assertFalse(r.valid());
    }

    @Test
    void sameSpeakerTwice_rejected() {
        // 上一发言 expert2,提案又是 expert2 且非补充 → 拒绝
        ValidationResult r = sched.validate(prop(2L, "举手", null, "又说。"), ongoing(), roster());
        assertFalse(r.valid());
    }

    @Test
    void consecutive_allowedWhenSupplementOwnLast() {
        // expert2 补充自己上一句(speech2)→ 允许
        ValidationResult r = sched.validate(prop(2L, "补充", 2L, "补充。"), ongoing(), roster());
        assertTrue(r.valid());
    }

    @Test
    void supplement_targetingNotOwnLast_rejected() {
        // B5b:连说 + 补充,但 target 非自己上一句(指向 host 的 speech1)→ 拒绝
        ValidationResult r = sched.validate(prop(2L, "补充", 1L, "补充。"), ongoing(), roster());
        assertFalse(r.valid());
    }

    @Test
    void speechOverTwoSentences_truncated() {
        String out = sched.enforceSentenceLimit("第一句。第二句。第三句。第四句。");
        assertEquals("第一句。第二句。", out);
    }

    @Test
    void emptyTranscript_hostOpening_valid() {
        ValidationResult r = sched.validate(prop(1L, "开场", null, "开场。"), new ArrayList<>(), roster());
        assertTrue(r.valid());
    }

    @Test
    void emptyTranscript_expertFirst_rejected() {
        ValidationResult r = sched.validate(prop(2L, "举手", null, "抢先。"), new ArrayList<>(), roster());
        assertFalse(r.valid());
    }

    @Test
    void speakerNotInRoster_rejected() {
        // B14:speakerId 不在阵容 → 非法
        ValidationResult r = sched.validate(prop(999L, "举手", null, "谁?"), ongoing(), roster());
        assertFalse(r.valid());
    }

    @Test
    void blankContent_rejected() {
        ValidationResult r = sched.validate(prop(3L, "举手", null, "   "), ongoing(), roster());
        assertFalse(r.valid());
    }

    @Test
    void shouldHostInterject_afterThreeToFourExpertTurns() {
        assertFalse(sched.shouldHostInterject(2));
        assertTrue(sched.shouldHostInterject(3));
    }
}
