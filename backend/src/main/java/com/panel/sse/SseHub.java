package com.panel.sse;

import com.panel.engine.DiscussionRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// 每讨论一组 emitter:广播 + 保活心跳 + 死连接清理。新连接先推 snapshot 再接实时。
@Component
public class SseHub {

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final DiscussionRegistry registry;

    public SseHub(DiscussionRegistry registry) {
        this.registry = registry;
    }

    public SseEmitter subscribe(long discussionId) {
        SseEmitter emitter = new SseEmitter(0L); // 0 = 不超时(靠心跳保活 + 客户端断开回调清理)
        List<SseEmitter> group = emitters.computeIfAbsent(discussionId, k -> new CopyOnWriteArrayList<>());
        emitter.onCompletion(() -> remove(discussionId, emitter));
        emitter.onTimeout(() -> remove(discussionId, emitter));
        emitter.onError(e -> remove(discussionId, emitter));
        // 与 broadcast 互斥:先发 snapshot 再入组,保证该连接收到的任何实时事件都严格晚于 snapshot(L8)
        synchronized (group) {
            try {
                emitter.send(SseEmitter.event().name("snapshot").data(registry.session(discussionId).snapshot()));
                group.add(emitter);
            } catch (IOException e) {
                // 客户端已断:不加入组
            }
        }
        return emitter;
    }

    public void broadcast(long discussionId, String event, Object data) {
        List<SseEmitter> group = emitters.get(discussionId);
        if (group == null) {
            return;
        }
        synchronized (group) {
            for (SseEmitter emitter : group) {
                try {
                    emitter.send(SseEmitter.event().name(event).data(data));
                } catch (Exception ex) {
                    remove(discussionId, emitter);
                }
            }
        }
    }

    @Scheduled(fixedRate = 20_000)
    public void heartbeat() {
        emitters.forEach((id, list) -> list.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().comment("ping"));
            } catch (Exception ex) {
                remove(id, emitter);
            }
        }));
    }

    // 供集成测试断言死连接清理(A9)
    public int connectionCount(long discussionId) {
        List<SseEmitter> list = emitters.get(discussionId);
        return list == null ? 0 : list.size();
    }

    private void remove(long discussionId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(discussionId);
        if (list != null) {
            list.remove(emitter);
        }
    }
}
