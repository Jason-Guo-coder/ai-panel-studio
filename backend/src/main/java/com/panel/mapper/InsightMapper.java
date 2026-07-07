package com.panel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panel.entity.Insight;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface InsightMapper extends BaseMapper<Insight> {

    // 供去重查询该讨论已有共识/分歧
    @Select("SELECT * FROM insight WHERE discussion_id = #{discussionId}")
    List<Insight> selectByDiscussion(long discussionId);
}
