package com.panel.support;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// 集成测试基座:target 下文件 SQLite + WAL(读不阻塞写、上下文间不销毁)+ 确定性 fake-ai;不碰 dev 库、零网络。
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("fake-ai")
public abstract class WebIntegrationTest {

    static final String DB = "target/it-test.db";

    static {
        // 套件开始清一次,保新鲜(schema IF NOT EXISTS 幂等重建)
        for (String suffix : new String[]{"", "-wal", "-shm"}) {
            try {
                Files.deleteIfExists(Path.of(DB + suffix));
            } catch (IOException ignored) {
            }
        }
    }

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",
                () -> "jdbc:sqlite:" + DB + "?journal_mode=WAL&foreign_keys=on&busy_timeout=5000");
        // 内存库不自动灌 seed,保测试确定性
        r.add("spring.sql.init.data-locations", () -> "");
    }
}
