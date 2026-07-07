package com.panel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

// 启动收敛:残留 running 讨论(上次进程崩留下的)标 interrupted;并打印 WAL 自检。
@Component
public class StartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

    private final JdbcTemplate jdbc;

    public StartupRunner(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        int fixed = jdbc.update("UPDATE discussion SET status='interrupted' WHERE status='running'");
        String journalMode = jdbc.queryForObject("PRAGMA journal_mode", String.class);
        log.info("启动收敛:{} 个残留 running 讨论标记为 interrupted;SQLite journal_mode={}", fixed, journalMode);
    }
}
