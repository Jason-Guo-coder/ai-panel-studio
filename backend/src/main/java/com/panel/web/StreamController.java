package com.panel.web;

import com.panel.sse.SseHub;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class StreamController {

    private final SseHub hub;

    public StreamController(SseHub hub) {
        this.hub = hub;
    }

    @GetMapping("/api/discussions/{id}/stream")
    public SseEmitter stream(@PathVariable long id) {
        return hub.subscribe(id);
    }
}
