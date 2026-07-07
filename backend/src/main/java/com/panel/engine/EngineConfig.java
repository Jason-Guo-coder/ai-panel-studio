package com.panel.engine;

import com.panel.ai.AiService;
import com.panel.mapper.DiscussionMapper;
import com.panel.mapper.SpeechMapper;
import com.panel.service.InsightExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EngineConfig {

    // DiscussionEngine 非 @Component(有 int 构造参数);此处装配并注入硬上限
    @Bean
    public DiscussionEngine discussionEngine(AiService ai, TurnScheduler scheduler, InsightExtractor insightExtractor,
                                             DiscussionMapper discussionMapper, SpeechMapper speechMapper,
                                             EventPublisher events,
                                             @Value("${panel.max-speeches:16}") int maxSpeeches) {
        return new DiscussionEngine(ai, scheduler, insightExtractor, discussionMapper, speechMapper, events, maxSpeeches);
    }
}
