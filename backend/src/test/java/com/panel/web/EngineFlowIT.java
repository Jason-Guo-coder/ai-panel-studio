package com.panel.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.panel.support.WebIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// A2 确认→running 交接起跑;A7 两讨论并行隔离(discussion_id 不串味)。fake-ai 驱动,零网络。
class EngineFlowIT extends WebIntegrationTest {

    @Autowired
    MockMvc mvc;

    final ObjectMapper om = new ObjectMapper();

    private long create(String topic) throws Exception {
        String body = mvc.perform(post("/api/discussions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"topic\":\"" + topic + "\",\"expertCount\":4}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(body).get("id").asLong();
    }

    private void confirm(long id) throws Exception {
        mvc.perform(post("/api/discussions/" + id + "/confirm")).andExpect(status().isAccepted());
    }

    private JsonNode detail(long id) throws Exception {
        String body = mvc.perform(get("/api/discussions/" + id)).andReturn().getResponse().getContentAsString();
        return om.readTree(body);
    }

    private JsonNode awaitFinished(long id) throws Exception {
        for (int i = 0; i < 100; i++) {
            JsonNode d = detail(id);
            if ("finished".equals(d.get("discussion").get("status").asText())) {
                return d;
            }
            Thread.sleep(150);
        }
        return detail(id);
    }

    private Set<Long> participantIds(JsonNode arr) {
        Set<Long> s = new HashSet<>();
        arr.forEach(p -> s.add(p.get("id").asLong()));
        return s;
    }

    @Test
    void confirm_transitionsToRunning_andLoopRuns() throws Exception {
        long id = create("话题X");
        confirm(id);
        JsonNode d = awaitFinished(id);
        assertNotEquals("generating", d.get("discussion").get("status").asText(), "确认后应离开 generating");
        assertTrue(d.get("speeches").size() >= 1, "引擎循环应写入发言");
    }

    @Test
    void twoDiscussions_isolated_noCrossContamination() throws Exception {
        long a = create("讨论A");
        long b = create("讨论B");
        confirm(a);
        confirm(b);
        JsonNode da = awaitFinished(a);
        JsonNode db = awaitFinished(b);

        Set<Long> pa = participantIds(da.get("participants"));
        Set<Long> pb = participantIds(db.get("participants"));
        assertTrue(Collections.disjoint(pa, pb), "两讨论参会者 id 不应重叠");

        for (JsonNode s : da.get("speeches")) {
            assertTrue(pa.contains(s.get("participantId").asLong()), "A 的发言人须属于 A");
        }
        for (JsonNode s : db.get("speeches")) {
            assertTrue(pb.contains(s.get("participantId").asLong()), "B 的发言人须属于 B");
        }
        for (JsonNode ins : da.get("insights")) {
            assertEquals(a, ins.get("discussionId").asLong(), "A 的共识分歧须挂在 A");
        }
        for (JsonNode ins : db.get("insights")) {
            assertEquals(b, ins.get("discussionId").asLong(), "B 的共识分歧须挂在 B");
        }
        assertTrue(da.get("speeches").size() >= 1 && db.get("speeches").size() >= 1);
    }
}
