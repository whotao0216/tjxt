package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.dto.course.CourseFullInfoDTO;
import com.tianji.api.dto.leanring.LearningLessonDTO;
import com.tianji.api.dto.leanring.LearningRecordDTO;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.exceptions.DbException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.LearningRecordFormDTO;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.enums.SectionType;
import com.tianji.learning.mapper.LearningRecordMapper;
import com.tianji.learning.service.ILearningLessonService;
import com.tianji.learning.service.ILearningRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lyh
 * @description 针对表【learning_record(学习记录表)】的数据库操作Service实现
 * @createDate 2024-04-07 17:22:16
 */
@Service
@RequiredArgsConstructor
public class LearningRecordServiceImpl extends ServiceImpl<LearningRecordMapper, LearningRecord>
        implements ILearningRecordService {

    private final ILearningLessonService lessonService;
    private final CourseClient courseClient;

    @Override
    public LearningLessonDTO queryLearningRecordByCourse(Long courseId) {
        // 1.获取登录用户
        Long userId = UserContext.getUser();
        // 2.查询课表
        LearningLesson lesson = lessonService.lambdaQuery()
                .eq(LearningLesson::getCourseId, courseId)
                .eq(LearningLesson::getUserId, userId)
                .one();
        if (lesson == null) {
            throw new BizIllegalException("该课程不在课表中");
        }
        // 3.查询学习记录
        // select * from xx where lesson_id = #{lessonId}
        List<LearningRecord> records = lambdaQuery()
                .eq(LearningRecord::getLessonId, lesson.getId()).list();
        // 4.封装结果
        LearningLessonDTO dto = new LearningLessonDTO();
        dto.setId(lesson.getId());
        dto.setLatestSectionId(lesson.getLatestSectionId());
        dto.setRecords(BeanUtils.copyList(records, LearningRecordDTO.class));
        return dto;
    }

    @Override
    public void addLearningRecord(LearningRecordFormDTO dto) {
        //1、获取当前用户
        Long userId = UserContext.getUser();
//        2、处理学习记录
        boolean finished = false;//代表本小节是否学完
        if (dto.getSectionType().equals(SectionType.VIDEO)) {
//        2.1、提交考试记录
            finished = handleExamRecord(userId, dto);
        } else {
//        2.2、提交视频播放记录
            finished = handleVideoRecord(userId, dto);
        }
//        3、处理课表数据
        handleLessonData(dto, finished);
    }

    //处理课表相关数据
    private void handleLessonData(LearningRecordFormDTO dto, boolean finished) {
        //1.查询课表learning_lesson
        LearningLesson lesson = lessonService.getById(dto.getLessonId());
        if (lesson == null) {
            throw new BizIllegalException("课表不存在");
        }
        //2. 判断是否第一次学完 finished是否为true
        boolean allFinished = false;//表示所有小节是否学完
        if (finished) {
            //3.远程调用课程服务获取课程信息
            CourseFullInfoDTO cinfo = courseClient.getCourseInfoById(lesson.getCourseId(), false, false);
            if (cinfo == null) {
                throw new BizIllegalException("课程不存在");
            }
            Integer sectionNum = cinfo.getSectionNum();
            //4.如果finished为true则判断是否课程所有小节全部学完
            allFinished = lesson.getLearnedSections() + 1 >= sectionNum;
        }
        //5.更新课表数据
        lessonService.lambdaUpdate()
                .set(lesson.getStatus() == LessonStatus.NOT_BEGIN, LearningLesson::getStatus, LessonStatus.LEARNING)
                .set(allFinished, LearningLesson::getStatus, LessonStatus.FINISHED)
                .set(LearningLesson::getLatestSectionId, dto.getSectionId())
                .set(LearningLesson::getLatestLearnTime, dto.getCommitTime())
                .setSql(finished, "learned_sections=learned_sections+1")
                .eq(LearningLesson::getId, lesson.getId())
                .update();
    }

    //处理播放记录
    private boolean handleVideoRecord(Long userId, LearningRecordFormDTO dto) {
        //查询学习记录
        LearningRecord record = lambdaQuery()
                .eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getLessonId, dto.getLessonId())
                .eq(LearningRecord::getSectionId, dto.getSectionId())
                .one();
        //判断学习记录是否存在
        if (record == null) {
            //不存在则新增学习记录
            LearningRecord learningRecord = BeanUtils.copyBean(dto, LearningRecord.class);
            learningRecord.setUserId(userId);
            boolean result = save(learningRecord);
            if (!result) {
                throw new DbException("新增学习记录失败");
            }
            return false;
        }
        //判断本小节是否第一次学完,finished 为true则第一次学完
        boolean finished = !record.getFinished() && dto.getMoment() << 1 >= dto.getDuration();
        boolean result = lambdaUpdate()
                .set(LearningRecord::getMoment, dto.getMoment())
                .set(finished, LearningRecord::getFinished, true)
                .set(finished, LearningRecord::getFinishTime, dto.getCommitTime())
                .eq(LearningRecord::getId, record.getId())
                .update();
        if (!result) {
            throw new DbException("更新视频学习记录失败");
        }
        return finished;
    }

    //处理考试记录
    private boolean handleExamRecord(Long userId, LearningRecordFormDTO dto) {
        LearningRecord learningRecord = BeanUtils.copyBean(dto, LearningRecord.class);
        learningRecord.setUserId(userId);
        learningRecord.setFinishTime(dto.getCommitTime());
        boolean result = save(learningRecord);
        if (!result) {
            throw new DbException("新增考试记录失败");
        }
        return true;
    }
}




