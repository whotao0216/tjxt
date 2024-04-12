package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.remark.RemarkClient;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.dto.ReplyDTO;
import com.tianji.learning.domain.po.InteractionQuestion;
import com.tianji.learning.domain.po.InteractionReply;
import com.tianji.learning.domain.query.ReplyPageQuery;
import com.tianji.learning.domain.vo.ReplyVO;
import com.tianji.learning.enums.QuestionStatus;
import com.tianji.learning.mapper.InteractionQuestionMapper;
import com.tianji.learning.mapper.InteractionReplyMapper;
import com.tianji.learning.service.IInteractionReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author lyh
 * @description 针对表【interaction_reply(互动问题的回答或评论)】的数据库操作Service实现
 * @createDate 2024-04-09 16:58:29
 */
@Service
@RequiredArgsConstructor
public class InteractionReplyServiceImpl extends ServiceImpl<InteractionReplyMapper, InteractionReply>
        implements IInteractionReplyService {

    private final InteractionQuestionMapper questionMapper;
    private final UserClient userClient;
    private final RemarkClient remarkClient;

    @Override
    public void saveReplyOrComment(ReplyDTO dto) {
        //获取当前用户
        Long userId = UserContext.getUser();

        //提交回答或评论
        InteractionReply reply = BeanUtils.copyBean(dto, InteractionReply.class);
        reply.setUserId(userId);
        save(reply);
        InteractionQuestion question = questionMapper.selectById(dto.getQuestionId());
        Long answerId = dto.getAnswerId();
        if (answerId != null) {
            //回答
            InteractionReply answerInfo = getById(answerId);
            answerInfo.setReplyTimes(answerInfo.getReplyTimes() + 1);
        } else {
            //评论
            question.setLatestAnswerId(userId);
            question.setAnswerTimes(question.getAnswerTimes() + 1);
        }
        if (dto.getIsStudent()) {
            question.setStatus(QuestionStatus.UN_CHECK);
        }
        questionMapper.updateById(question);
    }

    @Override
    public PageDTO<ReplyVO> queryReply(ReplyPageQuery query) {
        if (query.getAnswerId() == null && query.getQuestionId() == null) {
            throw new BadRequestException("问题id和回答id不能同事为空");
        }

        //查询interaction_reply表
        Page<InteractionReply> page = lambdaQuery().eq(query.getQuestionId() != null, InteractionReply::getQuestionId, query.getQuestionId())
                .eq(InteractionReply::getAnswerId, query.getAnswerId() == null ? 0L : query.getAnswerId())
                .eq(InteractionReply::getHidden, false)
                .page(query.toMpPage(
                        new OrderItem("liked_times", false),
                        new OrderItem("create_time", true)
                ));
        List<InteractionReply> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(0L, 0L);
        }

        Set<Long> uids = new HashSet<>();
        Set<Long> targetReplyIds = new HashSet<>();
        List<Long> answerIds = new ArrayList<>();
        for (InteractionReply record : records) {
            if (!record.getAnonymity()) {
                uids.add(record.getUserId());
                uids.add(record.getTargetReplyId());
            }
            if (record.getTargetReplyId() != null && record.getTargetReplyId() > 0) {
                targetReplyIds.add(record.getTargetReplyId());
                answerIds.add(record.getId());
            }
        }
        //查询回复目标的信息
        if (!targetReplyIds.isEmpty()) {
            List<InteractionReply> replies = listByIds(targetReplyIds);
            Set<Long> targetUserIds = replies.stream()
                    .filter(Predicate.not(InteractionReply::getAnonymity))
                    .map(InteractionReply::getUserId)
                    .collect(Collectors.toSet());
            uids.addAll(targetUserIds);
        }
        List<UserDTO> userDTOS = userClient.queryUserByIds(uids);
        Map<Long, UserDTO> userDTOMap = new HashMap<>();
        if (CollUtils.isNotEmpty(userDTOS)) {
            userDTOMap = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, c -> c));
        }
        // 3.4.查询用户点赞状态
        Set<Long> bizLiked = remarkClient.getLikedStatusByBizIds(answerIds);

        //package vo
        List<ReplyVO> replyVOS = new ArrayList<>();
        for (InteractionReply record : records) {
            ReplyVO replyVO = BeanUtils.copyBean(record, ReplyVO.class);
            if (!record.getAnonymity()) {
                UserDTO userDTO = userDTOMap.get(record.getUserId());
                if (userDTO != null) {
                    replyVO.setUserIcon(userDTO.getIcon());
                    replyVO.setUserName(userDTO.getUsername());
                    replyVO.setUserType(userDTO.getType());
                }
            }
            UserDTO targetUserDTO = userDTOMap.get(record.getTargetReplyId());
            if (targetUserDTO != null) {
                replyVO.setTargetUserName(targetUserDTO.getUsername());
            }
            replyVOS.add(replyVO);
        }

        return PageDTO.of(page, replyVOS);
    }
}




