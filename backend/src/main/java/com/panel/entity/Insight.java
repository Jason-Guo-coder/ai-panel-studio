package com.panel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("insight")
public class Insight {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long discussionId;
    private String type;          // consensus|divergence
    private String content;
    private String createdAt;
}
