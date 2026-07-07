package com.panel.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.panel.ai.AiService;
import com.panel.ai.dto.DraftMember;
import com.panel.ai.dto.RosterDraft;
import com.panel.support.WebIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// T1 冒烟:REST CRUD 打通(AiService 打桩,零网络)
class DiscussionApiIT extends WebIntegrationTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    AiService ai;

    final ObjectMapper om = new ObjectMapper();

    private void stubP1() {
        RosterDraft draft = new RosterDraft(
                new DraftMember("主持", "媒体人", "主持", "中立"),
                List.of(
                        new DraftMember("专家1", "职业1", "T1", "立场1"),
                        new DraftMember("专家2", "职业2", "T2", "立场2"),
                        new DraftMember("专家3", "职业3", "T3", "立场3"),
                        new DraftMember("专家4", "职业4", "T4", "立场4")));
        when(ai.draftRoster(anyString(), anyInt())).thenReturn(draft);
    }

    private long createDiscussion(String topic) throws Exception {
        String body = mvc.perform(post("/api/discussions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"topic\":\"" + topic + "\",\"expertCount\":4}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("generating"))
                .andExpect(jsonPath("$.participants.length()").value(5))
                .andReturn().getResponse().getContentAsString();
        JsonNode n = om.readTree(body);
        return n.get("id").asLong();
    }

    @Test
    void create_then_detail_returnsRosterAndHistory() throws Exception {
        stubP1();
        long id = createDiscussion("话题A");
        mvc.perform(get("/api/discussions/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discussion.topic").value("话题A"))
                .andExpect(jsonPath("$.participants.length()").value(5))
                .andExpect(jsonPath("$.speeches.length()").value(0));
    }

    @Test
    void list_containsCreated() throws Exception {
        stubP1();
        createDiscussion("话题B");
        mvc.perform(get("/api/discussions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void regenerate_replacesRoster() throws Exception {
        stubP1();
        long id = createDiscussion("话题C");
        mvc.perform(post("/api/discussions/" + id + "/regenerate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participants.length()").value(5));
    }

    @Test
    void detail_missing_returns404() throws Exception {
        mvc.perform(get("/api/discussions/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
