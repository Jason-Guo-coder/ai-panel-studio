package com.panel.engine;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 多讨论隔离:Map 的 key 即隔离边界。
@Component
public class DiscussionRegistry {

    private final Map<Long, DiscussionSession> sessions = new ConcurrentHashMap<>();

    public DiscussionSession session(long discussionId) {
        return sessions.computeIfAbsent(discussionId, k -> new DiscussionSession());
    }

    public void remove(long discussionId) {
        sessions.remove(discussionId);
    }
}
