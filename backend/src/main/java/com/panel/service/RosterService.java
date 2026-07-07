package com.panel.service;

import com.panel.ai.AiService;
import com.panel.entity.Participant;
import org.springframework.stereotype.Service;

import java.util.List;

// 嘉宾生成(P1):校验人数边界 → 调 P1 取人设 → 后端按调色板指派颜色 → 组装校验。RED 阶段桩:未实现。
@Service
public class RosterService {

    private final AiService ai;

    public RosterService(AiService ai) {
        this.ai = ai;
    }

    public List<Participant> generateRoster(String topic, Integer expertCount) {
        throw new UnsupportedOperationException("RED: not implemented");
    }
}
