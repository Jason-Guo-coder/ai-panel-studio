package com.panel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panel.entity.Discussion;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface DiscussionMapper extends BaseMapper<Discussion> {

    @Update("UPDATE discussion SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") long id, @Param("status") String status);

    @Update("UPDATE discussion SET summary = #{summary} WHERE id = #{id}")
    int updateSummary(@Param("id") long id, @Param("summary") String summary);
}
