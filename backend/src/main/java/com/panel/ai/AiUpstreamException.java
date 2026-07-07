package com.panel.ai;

// AI 调用/解析失败可控上抛
public class AiUpstreamException extends RuntimeException {
    public AiUpstreamException(String message) {
        super(message);
    }
}
