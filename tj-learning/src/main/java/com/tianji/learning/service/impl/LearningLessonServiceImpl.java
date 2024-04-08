package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.course.CatalogueClient;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.client.learning.LearningClient;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.course.CataSimpleInfoDTO;
import com.tianji.api.dto.course.CourseFullInfoDTO;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.DateUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.LearningPlanDTO;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.po.LearningRecord;
import com.tianji.learning.domain.vo.LearningLessonVO;
import com.tianji.learning.domain.vo.LearningPlanPageVO;
import com.tianji.learning.domain.vo.LearningPlanVO;
import com.tianji.learning.enums.LessonStatus;
import com.tianji.learning.enums.PlanStatus;
import com.tianji.learning.mapper.LearningLessonMapper;
import com.tianji.learning.mapper.LearningRecordMapper;
import com.tianji.learning.service.ILearningLessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
@Service
@RequiredArgsConstructor
@Slf4j
public class LearningLessonServiceImpl extends ServiceImpl<LearningLessonMapper, LearningLesson> implements ILearningLessonService {

    private final CourseClient courseClient;
    private final UserClient userClient;
    private final CatalogueClient catalogueClient;
    private final LearningClient learningClient;
    private final LearningRecordMapper learningRecordMapper;

    @Override
    @Transactional
    public void addUserLessons(Long userId, List<Long> courseIds) {
        // 1.查询课程有效期
        List<CourseSimpleInfoDTO> cInfoList = courseClient.getSimpleInfoList(courseIds);
        if (CollUtils.isEmpty(cInfoList)) {
            // 课程不存在，无法添加
            log.error("课程信息不存在，无法添加到课表");
            return;
        }
        // 2.循环遍历，处理LearningLesson数据
        List<LearningLesson> list = new ArrayList<>(cInfoList.size());
        for (CourseSimpleInfoDTO cInfo : cInfoList) {
            LearningLesson lesson = new LearningLesson();
            // 2.1.获取过期时间
            Integer validDuration = cInfo.getValidDuration();
            if (validDuration != null && validDuration > 0) {
                LocalDateTime now = LocalDateTime.now();
                lesson.setCreateTime(now);
                lesson.setExpireTime(now.plusMonths(validDuration));
            }
            // 2.2.填充userId和courseId
            lesson.setUserId(userId);
            lesson.setCourseId(cInfo.getId());
            list.add(lesson);
        }
        // 3.批量新增
        saveBatch(list);
    }

    @Override
    public PageDTO<LearningLessonVO> queryMyLessons(PageQuery query) {
        //获取登陆者
        Long userId = UserContext.getUser();
        //分页查询
        Page<LearningLesson> page = lambdaQuery().eq(LearningLesson::getUserId, userId).page(query.toMpPage("latest_learn_time", false));
        List<LearningLesson> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        //调用远程课程服务
        List<Long> courseIds = records.stream().map(LearningLesson::getCourseId).collect(Collectors.toList());
        List<CourseSimpleInfoDTO> cInfos = courseClient.getSimpleInfoList(courseIds);
        if (CollUtils.isEmpty(cInfos)) {
            throw new BizIllegalException("课程不存在");
        }
        //将课程集合转换为map
        Map<Long, CourseSimpleInfoDTO> infoDTOMap = cInfos.stream().collect(Collectors.toMap(CourseSimpleInfoDTO::getId, c -> c));

        //将po封装为vo
        ArrayList<LearningLessonVO> vos = new ArrayList<>();
        for (LearningLesson record : records) {
//            LearningLessonVO vo = BeanUtils.copyBean(record, LearningLessonVO.class);
            LearningLessonVO vo = new LearningLessonVO();
            BeanUtils.copyProperties(record, vo);
            CourseSimpleInfoDTO infoDTO = infoDTOMap.get(record.getCourseId());
            if (infoDTO != null) {
                vo.setCourseName(infoDTO.getName());
                vo.setCourseCoverUrl(infoDTO.getCoverUrl());
                vo.setSections(infoDTO.getSectionNum());
            }
            vos.add(vo);
        }

        return PageDTO.of(page, vos);
    }

    @Override
    public LearningLessonVO queryMyCurrentLesson() {
        //获取当前用户
        Long userId = UserContext.getUser();
        //查询当前用户正在学习课程，降序排序取第一条，
        LearningLesson lesson = lambdaQuery().eq(LearningLesson::getUserId, userId).eq(LearningLesson::getStatus, LessonStatus.LEARNING).orderByDesc(LearningLesson::getLatestLearnTime).last("limit 1").one();
        if (lesson == null) {
            return null;
        }
        //远程调用课程服务
        CourseFullInfoDTO cinfo = courseClient.getCourseInfoById(lesson.getCourseId(), false, false);
        if (cinfo == null) {
            throw new BizIllegalException("课程不存在");
        }
        //查询当前用户课表总课程数
        Integer count = lambdaQuery().eq(LearningLesson::getCourseId, userId).count();
        //远程调用课程小节服务，获取小节信息
        Long latestSectionId = lesson.getLatestSectionId();
        List<CataSimpleInfoDTO> cataSimpleInfoDTOS = catalogueClient.batchQueryCatalogue(CollUtils.singletonList(latestSectionId));
        if (CollUtils.isEmpty(cataSimpleInfoDTOS)) {
            throw new BizIllegalException("小节不存在");
        }
        //封装到vo
        //LearningLessonVO vo = com.tianji.common.utils.BeanUtils.copyBean(lesson, LearningLessonVO.class);
        LearningLessonVO vo = new LearningLessonVO();
        BeanUtils.copyProperties(lesson, vo);
        vo.setCourseName(cinfo.getName());
        vo.setCourseCoverUrl(cinfo.getCoverUrl());
        vo.setSections(cinfo.getSectionNum());
        vo.setCourseAmount(count);
        CataSimpleInfoDTO cataSimpleInfoDTO = cataSimpleInfoDTOS.get(0);
        vo.setLatestSectionIndex(cataSimpleInfoDTO.getCIndex());
        vo.setLatestSectionName(cataSimpleInfoDTO.getName());
        return vo;
    }

    @Override
    public Long isLessonValid(Long courseId) {
        Long userId = UserContext.getUser();
        LearningLesson lesson = lambdaQuery().eq(LearningLesson::getUserId, userId).eq(LearningLesson::getCourseId, courseId).one();

        if (lesson == null) {
            return null;
        }

        if (lesson.getExpireTime() != null && LocalDateTime.now().isAfter(lesson.getExpireTime())) {
            return null;
        }

        return lesson.getId();
    }

    @Override
    public LearningLessonVO queryLessonStatus(Long courseId) {
        //获取当前用户
        Long userId = UserContext.getUser();
        //获取课程信息
        LearningLesson lesson = lambdaQuery().eq(LearningLesson::getUserId, userId).eq(LearningLesson::getCourseId, courseId).one();

        if (lesson == null) {
            return null;
        }

        //封装vo
        LearningLessonVO vo = new LearningLessonVO();
        BeanUtils.copyProperties(lesson, vo);
        return vo;
    }

    @Override
    public Integer countLearningLessonByCourse(Long courseId) {
        Integer count = lambdaQuery().eq(LearningLesson::getCourseId, courseId).count();
        return count;
    }

    @Override
    public void createLearningPlan(LearningPlanDTO learningPlanDTO) {
//        1.获取当前用户
        Long userId = UserContext.getUser();
//            2.查询课表
        LearningLesson lesson = lambdaQuery().eq(LearningLesson::getUserId, userId).eq(LearningLesson::getCourseId, learningPlanDTO.getCourseId()).one();
        if (lesson == null) {
            throw new BizIllegalException("该课程未加入课表");
        }
//            3.修改课表
        lesson.setWeekFreq(learningPlanDTO.getFreq());
        lesson.setPlanStatus(LessonStatus.LEARNING);
        updateById(lesson);
    }

    @Override
    public LearningPlanPageVO queryMyPlans(PageQuery query) {
        //1.获取用户id
        Long userId = UserContext.getUser();
//   TODO 2.查询积分
        //    3.查询本周学习计划数据
        QueryWrapper<LearningLesson> queryWrapper = new QueryWrapper();
        queryWrapper.select("sum(week_freq) as plansTotal");
        queryWrapper.eq("user_id", userId);
        queryWrapper.in("status", LessonStatus.NOT_BEGIN, LessonStatus.LEARNING);
        queryWrapper.eq("plan_status", PlanStatus.PLAN_RUNNING);
        Map<String, Object> map = getMap(queryWrapper);
        Integer plansTotal = 0;
        if (map != null && map.get("plansTotal") != null) {
            plansTotal = Integer.valueOf(map.get("plansTotal").toString());
        }
//        4.查询本周已学习计划数据
        LocalDate now = LocalDate.now();
        LocalDateTime weekBeginTime = DateUtils.getWeekBeginTime(now);
        LocalDateTime weekEndTime = DateUtils.getWeekEndTime(now);
        Integer weekFinishedPlanNum = learningRecordMapper.selectCount(
                Wrappers.<LearningRecord>lambdaQuery()
                        .eq(LearningRecord::getUserId, userId)
                        .eq(LearningRecord::getFinished, true)
                        .between(LearningRecord::getFinishTime, weekBeginTime, weekEndTime)
        );
//        5.查询课表数据
        Page<LearningLesson> page = lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .in(LearningLesson::getStatus, LessonStatus.NOT_BEGIN, LessonStatus.LEARNING)
                .eq(LearningLesson::getPlanStatus, PlanStatus.PLAN_RUNNING)
                .page(query.toMpPage("latest_learn_time", false));
        List<LearningLesson> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            LearningPlanPageVO vo = new LearningPlanPageVO();
            vo.setPages(0L);
            vo.setTotal(0L);
            vo.setList(CollUtils.emptyList());
            return vo;
        }
//        6.远程调用课程服务获取课程信息
        Set<Long> courseIds = records.stream().map(LearningLesson::getCourseId).collect(Collectors.toSet());
        List<CourseSimpleInfoDTO> cinfos = courseClient.getSimpleInfoList(courseIds);
        Map<Long, CourseSimpleInfoDTO> cInfoMap = cinfos.stream().collect(Collectors.toMap(CourseSimpleInfoDTO::getId, c -> c));
        if (CollUtils.isEmpty(cinfos)) {
            throw new BizIllegalException("课程不存在");
        }
        //7.查询学习记录表，查询当前用户本周每一门课已学习小节数量
        QueryWrapper<LearningRecord> wrapper = new QueryWrapper<>();
        wrapper.select("lesson_id", "count(*) as userId");
        wrapper.eq("user_id", userId);
        wrapper.eq("finished", true);
        wrapper.between("finish_time", weekBeginTime, weekEndTime);
        wrapper.groupBy("lesson_id");
        List<LearningRecord> learningRecords = learningRecordMapper.selectList(wrapper);
        Map<Long, Long> courseWeekFinishedNumMap = learningRecords.stream().collect(Collectors.toMap(LearningRecord::getLessonId, c -> c.getUserId()));


        LearningPlanPageVO vo = new LearningPlanPageVO();
        vo.setWeekTotalPlan(plansTotal);
        vo.setWeekFinished(weekFinishedPlanNum);
        ArrayList<LearningPlanVO> listvo = new ArrayList<>();
        for (LearningLesson record : records) {
            LearningPlanVO learningPlanVO = com.tianji.common.utils.BeanUtils.copyBean(record, LearningPlanVO.class);
            CourseSimpleInfoDTO infoDTO = cInfoMap.get(record.getCourseId());
            if (infoDTO != null) {
                learningPlanVO.setCourseName(infoDTO.getName());
                learningPlanVO.setSections(infoDTO.getSectionNum());
            }
            learningPlanVO.setWeekLearnedSections(courseWeekFinishedNumMap.getOrDefault(record.getId(), 0L).intValue());
            listvo.add(learningPlanVO);
        }
        vo.setList(listvo);
        return vo;
    }
}