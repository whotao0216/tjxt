package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.dto.ReplyDTO;
import com.tianji.learning.domain.po.InteractionReply;
import com.tianji.learning.domain.query.ReplyPageQuery;
import com.tianji.learning.domain.vo.ReplyVO;

/**
 * @author lyh
 * @description 针对表【interaction_reply(互动问题的回答或评论)】的数据库操作Service
 * @createDate 2024-04-09 21:27:39
 */
public interface IInteractionReplyService extends IService<InteractionReply> {

    void saveReplyOrComment(ReplyDTO dto);

    PageDTO<ReplyVO> queryReply(ReplyPageQuery query);
}
