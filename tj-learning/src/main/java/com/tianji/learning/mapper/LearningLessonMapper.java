package com.tianji.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.learning.domain.po.LearningLesson;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lyh
 * @description 针对表【learning_lesson(学生课程表)】的数据库操作Mapper
 * @createDate 2024-04-06 16:05:12
 * @Entity com.tianji.learning.domain.po.LearningLesson
 */
@Mapper
public interface LearningLessonMapper extends BaseMapper<LearningLesson> {

}




