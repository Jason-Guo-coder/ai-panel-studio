package com.panel.service;

import com.panel.ai.dto.InsightDraft;
import com.panel.ai.dto.TurnProposal;
import com.panel.entity.Insight;
import com.panel.mapper.InsightMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 共识/分歧提炼:仅主持人回合产出;解析/去重(type+content)/实时入库。
@Service
public class InsightExtractor {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final InsightMapper insightMapper;

    public InsightExtractor(InsightMapper insightMapper) {
        this.insightMapper = insightMapper;
    }

    public void extract(TurnProposal proposal, boolean isHostTurn, long discussionId) {
        if (!isHostTurn) {
            return; // 仅主持人回合产出共识/分歧
        }
        List<InsightDraft> drafts = proposal.insights();
        if (drafts == null || drafts.isEmpty()) {
            return;
        }

        Set<String> seen = new HashSet<>();
        for (Insight existing : insightMapper.selectByDiscussion(discussionId)) {
            seen.add(key(existing.getType(), existing.getContent()));
        }

        for (InsightDraft d : drafts) {
            if (isBlank(d.type()) || isBlank(d.content())) {
                continue; // 跳过残缺条目
            }
            String k = key(d.type(), d.content());
            if (!seen.add(k)) {
                continue; // 去重(已入库或本轮已处理)
            }
            Insight insight = new Insight();
            insight.setDiscussionId(discussionId);
            insight.setType(d.type());
            insight.setContent(d.content());
            insight.setCreatedAt(LocalDateTime.now().format(TS));
            insightMapper.insert(insight); // 实时入库,不等收尾
        }
    }

    private String key(String type, String content) {
        return type + "" + content;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
