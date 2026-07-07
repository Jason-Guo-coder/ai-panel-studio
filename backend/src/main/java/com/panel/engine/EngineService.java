package com.panel.engine;

import com.panel.entity.Participant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;

// 有界线程池:并发活跃讨论上限(默认3)+ 排队;提交讨论到引擎循环(后端自驱)。
@Service
public class EngineService {

    private final DiscussionEngine engine;
    private final ThreadPoolTaskExecutor pool;

    public EngineService(DiscussionEngine engine,
                         @Value("${panel.max-concurrent-discussions:3}") int maxConcurrent) {
        this.engine = engine;
        this.pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(maxConcurrent);
        pool.setMaxPoolSize(maxConcurrent);
        pool.setQueueCapacity(64); // ponytail: 有界队列;超出直接拒绝,防堆积
        pool.setThreadNamePrefix("engine-");
        pool.initialize();
    }

    // 确认→提交 loop 的交接点后调用;引擎线程独占推进该讨论。
    public void submit(long discussionId, List<Participant> roster) {
        pool.execute(() -> engine.runDiscussion(discussionId, roster));
    }
}
