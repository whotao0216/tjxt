package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.learning.domain.po.InteractionReply;
import com.tianji.learning.mapper.InteractionReplyMapper;
import com.tianji.learning.service.IInteractionReplyService;
import org.springframework.stereotype.Service;

/**
 * @author lyh
 * @description 针对表【interaction_reply(互动问题的回答或评论)】的数据库操作Service实现
 * @createDate 2024-04-09 16:58:29
 */
@Service
public class InteractionReplyServiceImpl extends ServiceImpl<InteractionReplyMapper, InteractionReply>
        implements IInteractionReplyService {

}




