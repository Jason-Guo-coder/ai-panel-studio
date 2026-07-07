package com.panel.sse;

import java.util.List;

// SSE 事件 payload(对齐 docs/API.md §2)
public final class SsePayloads {
    private SsePayloads() {
    }

    public record ExpertSnap(Long participantId, String status, String focus) {
    }

    public record SnapshotPayload(Long currentSpeakerId, List<ExpertSnap> experts) {
    }

    public record StatusPayload(Long participantId, String status, String focus) {
    }

    public record SummaryPayload(String summary) {
    }

    public record FinishedPayload(long discussionId) {
    }

    public record ErrorPayload(String message) {
    }
}
