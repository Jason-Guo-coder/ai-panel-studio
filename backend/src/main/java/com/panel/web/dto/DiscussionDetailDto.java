package com.panel.web.dto;

import com.panel.entity.Discussion;
import com.panel.entity.Insight;
import com.panel.entity.Participant;
import com.panel.entity.Speech;

import java.util.List;

public record DiscussionDetailDto(Discussion discussion, List<Participant> participants,
                                  List<Speech> speeches, List<Insight> insights) {
}
