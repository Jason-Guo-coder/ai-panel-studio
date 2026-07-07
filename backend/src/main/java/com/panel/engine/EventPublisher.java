package com.panel.engine;

import com.panel.entity.Insight;
import com.panel.entity.Speech;

// SSE 事件出口(architecture §6 七种事件);测试用 mock 验证交互
public interface EventPublisher {
    void speech(long discussionId, Speech speech);

    void status(long discussionId, long participantId, String status, String focus);

    void insight(long discussionId, Insight insight);

    void summary(long discussionId, String summary);

    void finished(long discussionId);

    void error(long discussionId, String message);
}
