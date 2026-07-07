package com.panel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("speech")
public class Speech {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long discussionId;
    private Long participantId;
    private String content;
    private String reactionType;      // 开场|串联|追问|收尾|举手|抢答|补充|反驳
    private Long targetSpeechId;      // 仅反驳非空
    private Integer seq;
    private String createdAt;
}
