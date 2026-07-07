package com.panel.web.dto;

import com.panel.entity.Participant;

import java.util.List;

public record RosterResponseDto(Long id, String topic, String status, Integer expertCount,
                                List<Participant> participants) {
}
