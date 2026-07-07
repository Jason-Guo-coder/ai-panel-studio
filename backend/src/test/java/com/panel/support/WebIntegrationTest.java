package com.panel.support;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

// 集成测试基座:全内存 SQLite(shared-cache),池固定 1 连接保持 in-memory 库存活;不碰 dev 库。
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public abstract class WebIntegrationTest {

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> "jdbc:sqlite:file::memory:?cache=shared&foreign_keys=on");
        r.add("spring.datasource.hikari.maximum-pool-size", () -> "1");
        r.add("spring.datasource.hikari.minimum-idle", () -> "1");
        // 内存库不自动灌 seed,保测试确定性
        r.add("spring.sql.init.data-locations", () -> "");
    }
}
