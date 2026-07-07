package com.panel.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.panel.entity.Discussion;
import com.panel.entity.Insight;
import com.panel.entity.Participant;
import com.panel.entity.Speech;
import com.panel.mapper.DiscussionMapper;
import com.panel.mapper.InsightMapper;
import com.panel.mapper.ParticipantMapper;
import com.panel.mapper.SpeechMapper;
import com.panel.web.InvalidStateException;
import com.panel.web.NotFoundException;
import com.panel.web.dto.DiscussionDetailDto;
import com.panel.web.dto.RosterResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// 讨论 CRUD 与历史组装。generating 阶段由请求线程写 status(单写者交接前)。
@Service
public class DiscussionService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DiscussionMapper discussionMapper;
    private final ParticipantMapper participantMapper;
    private final SpeechMapper speechMapper;
    private final InsightMapper insightMapper;
    private final RosterService rosterService;

    public DiscussionService(DiscussionMapper discussionMapper, ParticipantMapper participantMapper,
                             SpeechMapper speechMapper, InsightMapper insightMapper, RosterService rosterService) {
        this.discussionMapper = discussionMapper;
        this.participantMapper = participantMapper;
        this.speechMapper = speechMapper;
        this.insightMapper = insightMapper;
        this.rosterService = rosterService;
    }

    public List<Discussion> list() {
        return discussionMapper.selectList(new QueryWrapper<Discussion>().orderByDesc("created_at"));
    }

    @Transactional
    public RosterResponseDto createWithRoster(String topic, Integer expertCount) {
        List<Participant> roster = rosterService.generateRoster(topic, expertCount); // 校验人数 + P1
        int experts = countExperts(roster);
        Discussion d = new Discussion();
        d.setTopic(topic);
        d.setStatus("generating");
        d.setExpertCount(experts);
        d.setCreatedAt(LocalDateTime.now().format(TS));
        discussionMapper.insert(d);
        persistRoster(d.getId(), roster);
        return new RosterResponseDto(d.getId(), topic, "generating", experts, roster);
    }

    @Transactional
    public RosterResponseDto regenerate(long id) {
        Discussion d = require(id);
        if (!"generating".equals(d.getStatus())) {
            throw new InvalidStateException("只有生成中的讨论可重新生成阵容");
        }
        List<Participant> roster = rosterService.generateRoster(d.getTopic(), d.getExpertCount());
        participantMapper.delete(new QueryWrapper<Participant>().eq("discussion_id", id));
        persistRoster(id, roster);
        return new RosterResponseDto(id, d.getTopic(), d.getStatus(), d.getExpertCount(), roster);
    }

    public DiscussionDetailDto detail(long id) {
        Discussion d = require(id);
        List<Participant> participants = participantMapper.selectList(
                new QueryWrapper<Participant>().eq("discussion_id", id).orderByAsc("id"));
        List<Speech> speeches = speechMapper.selectList(
                new QueryWrapper<Speech>().eq("discussion_id", id).orderByAsc("seq"));
        List<Insight> insights = insightMapper.selectList(
                new QueryWrapper<Insight>().eq("discussion_id", id).orderByAsc("created_at"));
        return new DiscussionDetailDto(d, participants, speeches, insights);
    }

    public Discussion require(long id) {
        Discussion d = discussionMapper.selectById(id);
        if (d == null) {
            throw new NotFoundException("讨论不存在:" + id);
        }
        return d;
    }

    private void persistRoster(Long discussionId, List<Participant> roster) {
        for (Participant p : roster) {
            p.setDiscussionId(discussionId);
            participantMapper.insert(p);
        }
    }

    private int countExperts(List<Participant> roster) {
        return (int) roster.stream().filter(p -> "expert".equals(p.getRole())).count();
    }
}
