package com.panel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("participant")
public class Participant {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long discussionId;
    private String role;          // host|expert
    private String name;
    private String profession;
    private String title;
    private String stance;
    private String color;
}
