package com.panel.ai.dto;

import java.util.List;

// P1 阵容草稿:1 主持 + N 专家人设(无颜色)
public record RosterDraft(DraftMember host, List<DraftMember> experts) {
}
