package com.panel.ai.dto;

// P1 产出的人设草稿(不含颜色;颜色一律由后端按调色板指派)
public record DraftMember(String name, String profession, String title, String stance) {
}
