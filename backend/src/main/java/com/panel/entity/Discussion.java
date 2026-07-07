package com.panel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("discussion")
public class Discussion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String topic;
    private String status;        // generating|running|finished|interrupted
    private Integer expertCount;
    private String summary;
    private String createdAt;
}
