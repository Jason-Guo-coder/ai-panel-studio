-- AI 圆桌讨论 MVP · 数据库结构
-- SQLite。与 docs/architecture.md 的 erDiagram 一致。
-- 隔离边界:除 discussion 外每表带 discussion_id。

PRAGMA journal_mode = WAL;   -- 多引擎线程写 + 请求线程读,读不阻塞写
PRAGMA foreign_keys = ON;

-- 讨论(一场圆桌)
CREATE TABLE IF NOT EXISTS discussion (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    topic        TEXT    NOT NULL,
    status       TEXT    NOT NULL DEFAULT 'generating',  -- generating|running|finished|interrupted
    expert_count INTEGER NOT NULL DEFAULT 4,
    summary      TEXT,                                    -- 结束时主持人总结,独立读取
    created_at   TEXT    NOT NULL DEFAULT (datetime('now'))
);

-- 参会者(主持人 + 专家,合表靠 role 区分)
CREATE TABLE IF NOT EXISTS participant (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    discussion_id INTEGER NOT NULL,
    role          TEXT    NOT NULL,   -- host|expert
    name          TEXT    NOT NULL,
    profession    TEXT    NOT NULL,
    title         TEXT    NOT NULL,
    stance        TEXT    NOT NULL,
    color         TEXT    NOT NULL,   -- 专属色标 #RRGGBB
    FOREIGN KEY (discussion_id) REFERENCES discussion(id) ON DELETE CASCADE
);

-- 发言(Transcript 每一行)
CREATE TABLE IF NOT EXISTS speech (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    discussion_id    INTEGER NOT NULL,
    participant_id   INTEGER NOT NULL,
    content          TEXT    NOT NULL,             -- 1-2 句
    reaction_type    TEXT    NOT NULL,             -- 开场|串联|追问|收尾|举手|抢答|补充|反驳
    target_speech_id INTEGER,                      -- 仅反驳非空;调度用,不渲染
    seq              INTEGER NOT NULL,             -- 讨论内自增,保回放顺序
    created_at       TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (discussion_id)    REFERENCES discussion(id)  ON DELETE CASCADE,
    FOREIGN KEY (participant_id)   REFERENCES participant(id),
    FOREIGN KEY (target_speech_id) REFERENCES speech(id)
);

-- 共识/分歧(合表靠 type 区分,增量 append)
CREATE TABLE IF NOT EXISTS insight (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    discussion_id INTEGER NOT NULL,
    type          TEXT    NOT NULL,   -- consensus|divergence
    content       TEXT    NOT NULL,
    created_at    TEXT    NOT NULL DEFAULT (datetime('now')),  -- 实时区按时间序渲染
    FOREIGN KEY (discussion_id) REFERENCES discussion(id) ON DELETE CASCADE
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_speech_disc_seq      ON speech(discussion_id, seq);
CREATE INDEX IF NOT EXISTS idx_insight_disc_created ON insight(discussion_id, created_at);
CREATE INDEX IF NOT EXISTS idx_participant_disc     ON participant(discussion_id);
