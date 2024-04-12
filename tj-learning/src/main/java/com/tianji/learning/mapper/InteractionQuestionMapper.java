package com.tianji.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.learning.domain.po.InteractionQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lyh
 * @description 针对表【interaction_question(互动提问的问题表)】的数据库操作Mapper
 * @createDate 2024-04-09 21:27:39
 * @Entity com.tianji.learning.domain.po.InteractionQuestion
 */
@Mapper
public interface InteractionQuestionMapper extends BaseMapper<InteractionQuestion> {

}




