package com.tianji.learning.controller;

import com.tianji.common.domain.dto.PageDTO;
import com.tianji.learning.domain.dto.ReplyDTO;
import com.tianji.learning.domain.query.ReplyPageQuery;
import com.tianji.learning.domain.vo.ReplyVO;
import com.tianji.learning.service.IInteractionReplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@Api(tags = "评论相关接口")
@RequestMapping("/replies")
public class InteractionReplyController {

    private final IInteractionReplyService replyService;

    @ApiOperation("新增回答或评论")
    @PostMapping
    public void saveReplyOrComment(@RequestBody @Validated ReplyDTO dto) {
        replyService.saveReplyOrComment(dto);
    }

    @ApiOperation("分页查询回答或评论列表")
    @GetMapping("/page")
    public PageDTO<ReplyVO> queryReply(ReplyPageQuery query) {
        return replyService.queryReply(query);
    }
}
