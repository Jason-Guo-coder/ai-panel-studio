package com.panel.ai;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

// Deepseek(OpenAI 兼容)底层客户端。Key 只从环境变量/配置读,绝不硬编码,也不入日志。
// 只取 message.content;不读 reasoning_content(推理模型 CoT 不泄漏)。
@Component
public class DeepseekClient {

    private static final Logger log = LoggerFactory.getLogger(DeepseekClient.class);

    private final RestClient http;
    private final String model;

    public DeepseekClient(@Value("${deepseek.base-url}") String baseUrl,
                          @Value("${deepseek.api-key}") String apiKey,
                          @Value("${deepseek.model}") String model) {
        this.model = model;
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(Duration.ofSeconds(15));
        rf.setReadTimeout(Duration.ofSeconds(180)); // 推理模型可能较慢
        this.http = RestClient.builder()
                .requestFactory(rf)
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
            logUsage(resp.path("usage"));
            // 仅取 message.content;绝不读 reasoning_content
            return resp.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new AiUpstreamException("Deepseek 调用失败:" + e.getMessage());
        }
    }

    // 记账用:分列 prompt/completion/reasoning tokens(不含任何内容或 key)
    private void logUsage(JsonNode usage) {
        if (usage == null || usage.isMissingNode()) {
            return;
        }
        long prompt = usage.path("prompt_tokens").asLong();
        long completion = usage.path("completion_tokens").asLong();
        long reasoning = usage.path("completion_tokens_details").path("reasoning_tokens").asLong();
        log.info("DEEPSEEK_USAGE prompt={} completion={} reasoning={}", prompt, completion, reasoning);
    }
}
