//package com.tianji.learning.mq;
//
//import com.tianji.api.msg.LikedTimesDTO;
//import com.tianji.common.constants.MqConstants;
//import com.tianji.learning.domain.po.InteractionReply;
//import com.tianji.learning.service.IInteractionReplyService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.amqp.rabbit.annotation.Exchange;
//import org.springframework.amqp.rabbit.annotation.Queue;
//import org.springframework.amqp.rabbit.annotation.QueueBinding;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@RequiredArgsConstructor
//@Component
//public class LikedRecordListener {
//
//    private final IInteractionReplyService replyService;
//
//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(value = "qa.liked.times.queue", durable = "true"),
//            exchange = @Exchange(value = MqConstants.Exchange.LIKE_RECORD_EXCHANGE),
//            key = MqConstants.Key.QA_LIKED_TIMES_KEY))
//    public void onMsg(List<LikedTimesDTO> list) {
//        List<InteractionReply> replyList = new ArrayList<>();
//        for (LikedTimesDTO dto : list) {
//            InteractionReply reply = new InteractionReply();
//            reply.setLikedTimes(dto.getLikedTimes());
//            reply.setId(dto.getBizId());
//            replyList.add(reply);
//        }
//        replyService.updateBatchById(replyList);
//    }
//}
