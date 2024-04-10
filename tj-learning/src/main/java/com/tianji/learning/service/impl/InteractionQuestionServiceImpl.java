package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.cache.CategoryCache;
import com.tianji.api.client.course.CatalogueClient;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.client.search.SearchClient;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.course.CataSimpleInfoDTO;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.QuestionFormDTO;
import com.tianji.learning.domain.po.InteractionQuestion;
import com.tianji.learning.domain.po.InteractionReply;
import com.tianji.learning.domain.query.QuestionAdminPageQuery;
import com.tianji.learning.domain.query.QuestionPageQuery;
import com.tianji.learning.domain.vo.QuestionAdminVO;
import com.tianji.learning.domain.vo.QuestionVO;
import com.tianji.learning.mapper.InteractionQuestionMapper;
import com.tianji.learning.service.IInteractionQuestionService;
import com.tianji.learning.service.IInteractionReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author lyh
 * @description 针对表【interaction_question(互动提问的问题表)】的数据库操作Service实现
 * @createDate 2024-04-09 16:55:11
 */
@Service
@RequiredArgsConstructor
public class InteractionQuestionServiceImpl extends ServiceImpl<InteractionQuestionMapper, InteractionQuestion>
        implements IInteractionQuestionService {

    private final UserClient userClient;
    private final IInteractionReplyService replyService;
    private final CourseClient courseClient;
    private final SearchClient searchClient;
    private final CatalogueClient catalogueClient;
    private final CategoryCache categoryCache;

    @Override
    public void saveQuestion(QuestionFormDTO dto) {
        //获取当前用户数据
        Long userId = UserContext.getUser();
        //dto to po
        InteractionQuestion question = BeanUtils.copyBean(dto, InteractionQuestion.class);
        question.setUserId(userId);
        //save
        save(question);
    }

    @Override
    public void editQuestion(Long id, QuestionFormDTO dto) {
        if (StringUtils.isBlank(dto.getTitle()) || StringUtils.isBlank(dto.getDescription()) || dto.getAnonymity()) {
            throw new BadRequestException("非法参数");
        }

        lambdaUpdate()
                .eq(InteractionQuestion::getId, id)
                .eq(InteractionQuestion::getUserId, UserContext.getUser())
                .set(InteractionQuestion::getTitle, dto.getTitle())
                .set(InteractionQuestion::getDescription, dto.getDescription())
                .set(InteractionQuestion::getAnonymity, dto.getAnonymity());
    }

    @Override
    public PageDTO<QuestionVO> queryQuestionPage(QuestionPageQuery query) {
        //校验courseId
        if (query.getCourseId() == null) throw new BadRequestException("课程id不能为空");
        //获取当前用户
        Long userId = UserContext.getUser();
        //分页查询互动问题表interaction_question 条件：courseId  onlyMine is true 加userId 小节id不为空 hidden为false
        Page<InteractionQuestion> page = lambdaQuery()
                .select(InteractionQuestion.class, new Predicate<TableFieldInfo>() {
                    @Override
                    public boolean test(TableFieldInfo tableFieldInfo) {
                        return !tableFieldInfo.getProperty().equals("description");
                    }
                })//排除不需要查询的description字段
                .eq(query.getOnlyMine(), InteractionQuestion::getUserId, userId)
                .eq(InteractionQuestion::getCourseId, query.getCourseId())
                .eq(query.getSectionId() != null, InteractionQuestion::getSectionId, query.getSectionId())
                .eq(InteractionQuestion::getHidden, false)
                .page(query.toMpPageDefaultSortByCreateTimeDesc());

        List<InteractionQuestion> records = page.getRecords();
        if (CollUtils.isEmpty(records)) return PageDTO.empty(page);
//        根据回答id获取回答信息
        Set<Long> latestAnswerIds = records.stream()
                .filter(new Predicate<InteractionQuestion>() {
                    @Override
                    public boolean test(InteractionQuestion interactionQuestion) {
                        return interactionQuestion.getLatestAnswerId() != null;
                    }
                })
                .map(InteractionQuestion::getLatestAnswerId)
                .collect(Collectors.toSet());
        Set<Long> userIds = new HashSet<>();//互动问题用户的id集合
        for (InteractionQuestion record : records) {
            if (!record.getAnonymity()) {
                //非匿名用户
                userIds.add(record.getUserId());
            }
        }
        Map<Long, InteractionReply> replyMap = new HashMap<>();
        if (CollUtils.isNotEmpty(latestAnswerIds)) {
            //List<InteractionReply> replies = replyService.listByIds(latestAnswerIds);
            List<InteractionReply> replies = replyService.list(Wrappers.<InteractionReply>lambdaQuery()
                    .in(InteractionReply::getId, latestAnswerIds)
                    .eq(InteractionReply::getHidden, false));
            for (InteractionReply reply : replies) {
                if (!reply.getAnonymity()) {
                    userIds.add(reply.getUserId());
                }
                replyMap.put(reply.getId(), reply);
            }
        }
        //调用用户服务 获取用户信息
        Map<Long, UserDTO> userDTOMap = userClient.queryUserByIds(userIds).stream().collect(Collectors.toMap(UserDTO::getId, c -> c));
        //封装vo返回

        List<QuestionVO> voList = new ArrayList<>();
        for (InteractionQuestion record : records) {
            QuestionVO questionVO = BeanUtils.copyBean(record, QuestionVO.class);
            if (!questionVO.getAnonymity()) {
                UserDTO userDTO = userDTOMap.get(record.getUserId());
                if (userDTO != null) {
                    questionVO.setUserName(userDTO.getName());
                    questionVO.setUserIcon(userDTO.getIcon());
                }
            }
            InteractionReply reply = replyMap.get(record.getLatestAnswerId());
            if (reply != null) {
                if (!reply.getAnonymity()) {
                    UserDTO userDTO = userDTOMap.get(reply.getUserId());
                    if (userDTO != null) {
                        questionVO.setLatestReplyUser(userDTO.getName());
                    }
                }
                questionVO.setLatestReplyContent(reply.getContent());
            }
            voList.add(questionVO);
        }

        return PageDTO.of(page, voList);
    }

    @Override
    public QuestionVO queryQuestionById(Long id) {
        InteractionQuestion question = getById(id);
        if (question == null) throw new BadRequestException("问题不存在");
        if (question.getHidden()) {
            return null;
        }
        QuestionVO questionVO = BeanUtils.copyBean(question, QuestionVO.class);
        if (!question.getAnonymity()) {
            UserDTO userDTO = userClient.queryUserById(question.getUserId());
            questionVO.setUserIcon(userDTO.getIcon());
            questionVO.setUserName(userDTO.getName());
        }
        return questionVO;
    }

    @Override
    public PageDTO<QuestionAdminVO> queryQuestionAdminVOPage(QuestionAdminPageQuery query) {
        //从es中获取课程的课程id
        String courseName = query.getCourseName();
        List<Long> cids = new ArrayList<>();
        if (StringUtils.isNotBlank(courseName)) {
            cids = searchClient.queryCoursesIdByName(courseName);
            if (CollUtils.isEmpty(cids)) {
                return PageDTO.empty(0L, 0L);
            }
        }

        //分页查询互动问题表 根据前端条件查询
        Page<InteractionQuestion> page = lambdaQuery()
                .in(CollUtils.isNotEmpty(cids), InteractionQuestion::getCourseId, cids)
                .eq(query.getStatus() != null, InteractionQuestion::getStatus, query.getStatus())
                .between(query.getBeginTime() != null && query.getEndTime() != null,
                        InteractionQuestion::getCreateTime, query.getBeginTime(), query.getEndTime())
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        List<InteractionQuestion> records = page.getRecords();
        if (CollUtils.isEmpty(records)) return PageDTO.empty(0L, 0L);

        Set<Long> uids = new HashSet<>();//userid set
        Set<Long> courseIds = new HashSet<>();
        Set<Long> chapterAndSectionIds = new HashSet<>();//章和节的id集合

        for (InteractionQuestion record : records) {
            uids.add(record.getUserId());
            courseIds.add(record.getCourseId());
            chapterAndSectionIds.add(record.getChapterId());
            chapterAndSectionIds.add(record.getSectionId());
        }
        //get users information
        List<UserDTO> userDTOS = userClient.queryUserByIds(uids);
        if (CollUtils.isEmpty(userDTOS)) {
            throw new BizIllegalException("用户不存在");
        }
        Map<Long, UserDTO> userDTOMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, c -> c));

        //get course information
        List<CourseSimpleInfoDTO> simpleInfoList = courseClient.getSimpleInfoList(courseIds);
        if (CollUtils.isEmpty(simpleInfoList)) {
            throw new BizIllegalException("课程不存在");
        }
        Map<Long, CourseSimpleInfoDTO> cInfoMap = simpleInfoList.stream().collect(Collectors.toMap(CourseSimpleInfoDTO::getId, c -> c));

        //get chapter and section information
        List<CataSimpleInfoDTO> cataSimpleInfoDTOS = catalogueClient.batchQueryCatalogue(chapterAndSectionIds);
        if (CollUtils.isEmpty(cataSimpleInfoDTOS)) {
            throw new BizIllegalException("章和节信息不存在");
        }
        Map<Long, String> cataInfoDTO = cataSimpleInfoDTOS.stream().collect(Collectors.toMap(CataSimpleInfoDTO::getId, c -> c.getName()));

        //封装vo
        List<QuestionAdminVO> voList = new ArrayList<>();
        for (InteractionQuestion record : records) {
            QuestionAdminVO questionAdminVO = BeanUtils.copyBean(record, QuestionAdminVO.class);
            UserDTO userDTO = userDTOMap.get(record.getUserId());
            if (userDTO != null) {
                questionAdminVO.setUserName(userDTO.getName());
            }
            CourseSimpleInfoDTO cinfoDTO = cInfoMap.get(record.getCourseId());
            if (cinfoDTO != null) {
                questionAdminVO.setCourseName(cinfoDTO.getName());

                //设置课程的三级分类
                String categoryNames = categoryCache.getCategoryNames(cinfoDTO.getCategoryIds());
                questionAdminVO.setCategoryName(categoryNames);
            }
            questionAdminVO.setChapterName(cataInfoDTO.get(record.getChapterId()));
            questionAdminVO.setSectionName(cataInfoDTO.get(record.getSectionId()));

            voList.add(questionAdminVO);
        }

        return PageDTO.of(page, voList);
    }

}