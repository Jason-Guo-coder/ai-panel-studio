package com.panel.ai;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

// Deepseek(OpenAI 兼容)底层客户端。Key 只从环境变量/配置读,绝不硬编码。
// 真实调用留阶段 5 联调;单测对 AiService 打桩,不经此类。
@Component
public class DeepseekClient {

    private final RestClient http;
    private final String model;

    public DeepseekClient(@Value("${deepseek.base-url}") String baseUrl,
                          @Value("${deepseek.api-key}") String apiKey,
                          @Value("${deepseek.model}") String model) {
        this.model = model;
        this.http = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    // 单轮对话补全,返回模型文本
    public String chat(String system, String user) {
        Map<String, Object> body = Map.of(
                "model", model,
                "stream", false,
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", user)));
        try {
            JsonNode resp = http.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            return resp.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new AiUpstreamException("Deepseek 调用失败:" + e.getMessage());
        }
    }
}
