package com.tianji.remark.task;

import com.tianji.remark.service.ILikedRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LikedTimesCheckTask {

    public static final List<String> BIZ_TYPES = List.of("QA", "NOTE");//业务类型
    public static final int MAX_BIZ_SIZE = 30;//每次取的biz数
    private final ILikedRecordService likedRecordService;

    //20s执行一次 将Redis中业务类型 和业务点赞总数发送到mq
    @Scheduled(fixedDelay = 20000)
    //@Scheduled(cron = "0/20 * * * * ?")
    public void checkLikedTiems() {
        for (String bizType : BIZ_TYPES) {
            likedRecordService.readLikedTimesAndSendMessage(bizType, MAX_BIZ_SIZE);
        }
    }
}
