package com.tianji.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.learning.domain.po.InteractionReply;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lyh
 * @description 针对表【interaction_reply(互动问题的回答或评论)】的数据库操作Mapper
 * @createDate 2024-04-09 21:27:39
 * @Entity com.tianji.learning.domain.po.InteractionReply
 */
@Mapper
public interface InteractionReplyMapper extends BaseMapper<InteractionReply> {

}




