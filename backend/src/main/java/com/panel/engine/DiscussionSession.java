package com.panel.engine;

import com.panel.sse.SsePayloads.ExpertSnap;
import com.panel.sse.SsePayloads.SnapshotPayload;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 每讨论内存态:当前发言人 + 每专家 status/focus。snapshot 数据源(新连接接入时重建小窗)。
public class DiscussionSession {

    private record ExpertState(String status, String focus) {
    }

    private volatile Long currentSpeakerId;
    private final Map<Long, ExpertState> experts = new ConcurrentHashMap<>();

    public void setCurrentSpeaker(Long participantId) {
        this.currentSpeakerId = participantId;
    }

    public void setExpert(long participantId, String status, String focus) {
        experts.put(participantId, new ExpertState(status, focus));
    }

    public SnapshotPayload snapshot() {
        List<ExpertSnap> list = experts.entrySet().stream()
                .map(e -> new ExpertSnap(e.getKey(), e.getValue().status(), e.getValue().focus()))
                .toList();
        return new SnapshotPayload(currentSpeakerId, list);
    }
}
