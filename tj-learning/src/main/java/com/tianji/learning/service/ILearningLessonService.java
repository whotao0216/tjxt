package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.dto.LearningPlanDTO;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.vo.LearningLessonVO;
import com.tianji.learning.domain.vo.LearningPlanPageVO;

import java.util.List;

/**
 * @author lyh
 * @description 针对表【learning_lesson(学生课程表)】的数据库操作Service
 * @createDate 2024-04-06 16:05:12
 */
public interface ILearningLessonService extends IService<LearningLesson> {

    void addUserLessons(Long userId, List<Long> courseIds);

    PageDTO<LearningLessonVO> queryMyLessons(PageQuery query);

    LearningLessonVO queryMyCurrentLesson();

    Long isLessonValid(Long courseId);

    LearningLessonVO queryLessonStatus(Long courseId);

    Integer countLearningLessonByCourse(Long courseId);

    void createLearningPlan(LearningPlanDTO learningPlanDTO);

    LearningPlanPageVO queryMyPlans(PageQuery query);
}
