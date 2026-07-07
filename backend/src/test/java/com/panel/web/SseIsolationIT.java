package com.panel.web;

import com.panel.entity.Speech;
import com.panel.sse.SseHub;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

// A8 新连接 snapshot 先于实时(真 HTTP 读首个事件);A9 断连后 emitter 移除(经广播失败清理路径)。
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("fake-ai")
class SseIsolationIT {

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",
                () -> "jdbc:sqlite:target/it-test.db?journal_mode=WAL&foreign_keys=on&busy_timeout=5000");
        r.add("spring.sql.init.data-locations", () -> "");
    }

    @LocalServerPort
    int port;

    @Autowired
    SseHub hub;

    @Test
    @Timeout(15)
    void newConnection_firstEventIsSnapshot() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder(
                URI.create("http://localhost:" + port + "/api/discussions/777/stream")).GET().build();
        HttpResponse<Stream<String>> resp = client.send(req, HttpResponse.BodyHandlers.ofLines());

        // 首个 event: 行必须是 snapshot(接入即先推快照,先于任何实时事件)
        Optional<String> firstEvent = resp.body()
                .map(String::trim)
                .filter(line -> line.startsWith("event:"))
                .findFirst();
        assertTrue(firstEvent.isPresent(), "应至少收到一个事件");
        assertEquals("event:snapshot", firstEvent.get(), "首个事件必须是 snapshot");
    }

    @Test
    void disconnect_removesEmitter() {
        long id = 888L;
        SseEmitter emitter = hub.subscribe(id);
        assertEquals(1, hub.connectionCount(id), "订阅后应有 1 个连接");

        // 客户端断开 = 连接失效;下次广播 send 失败即清理该死连接
        emitter.complete();
        Speech s = new Speech();
        s.setId(1L);
        s.setParticipantId(2L);
        s.setContent("x");
        s.setSeq(1);
        hub.broadcast(id, "speech", s);

        assertEquals(0, hub.connectionCount(id), "死连接应被移除");
    }
}
