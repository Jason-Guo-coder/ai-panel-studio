package com.panel.sse;

import com.panel.engine.DiscussionRegistry;
import com.panel.engine.EventPublisher;
import com.panel.entity.Insight;
import com.panel.entity.Speech;
import com.panel.sse.SsePayloads.ErrorPayload;
import com.panel.sse.SsePayloads.FinishedPayload;
import com.panel.sse.SsePayloads.StatusPayload;
import com.panel.sse.SsePayloads.SummaryPayload;
import org.springframework.stereotype.Component;

// EventPublisher 的 SSE 实现:广播七类事件,并把 status/currentSpeaker 落入 registry liveState(供 snapshot)。
@Component
public class SseEventPublisher implements EventPublisher {

    private final SseHub hub;
    private final DiscussionRegistry registry;

    public SseEventPublisher(SseHub hub, DiscussionRegistry registry) {
        this.hub = hub;
        this.registry = registry;
    }

    @Override
    public void speech(long discussionId, Speech speech) {
        registry.session(discussionId).setCurrentSpeaker(speech.getParticipantId());
        hub.broadcast(discussionId, "speech", speech);
    }

    @Override
    public void status(long discussionId, long participantId, String status, String focus) {
        registry.session(discussionId).setExpert(participantId, status, focus);
        hub.broadcast(discussionId, "status", new StatusPayload(participantId, status, focus));
    }

    @Override
    public void insight(long discussionId, Insight insight) {
        hub.broadcast(discussionId, "insight", insight);
    }

    @Override
    public void summary(long discussionId, String summary) {
        hub.broadcast(discussionId, "summary", new SummaryPayload(summary));
    }

    @Override
    public void finished(long discussionId) {
        hub.broadcast(discussionId, "finished", new FinishedPayload(discussionId));
    }

    @Override
    public void error(long discussionId, String message) {
        hub.broadcast(discussionId, "error", new ErrorPayload(message));
    }
}
