package com.tianji.learning.service.impl;

import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.constants.RedisConstants;
import com.tianji.learning.domain.vo.SignResultVO;
import com.tianji.learning.service.ISignRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SignRecordServiceImpl implements ISignRecordService {
    private final StringRedisTemplate redisTemplate;
    //private final RabbitMqHelper mqHelper;

    @Override
    public SignResultVO addSignRecord() {
        //获取用户id
        Long userId = UserContext.getUser();
        //拼接key
        LocalDate now = LocalDate.now();
        String yyyyMM = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = RedisConstants.SIGN_RECORD_KEY_PREFIX + userId.toString() + yyyyMM;
        //用bitset命令，将签到记录保存到Redis的bitmap中
        Boolean setBit = redisTemplate.opsForValue().setBit(key, now.getDayOfMonth() - 1, true);
        if (Boolean.TRUE.equals(setBit)) {
            throw new BizIllegalException("不能重复签到");
        }
        //计算连续签到天数
        int days = countSignDays(key, now.getDayOfMonth());
        //计算奖励积分
        int rewardPoints = 0;
        switch (days) {
            case 7:
                rewardPoints = 10;
                break;
            case 14:
                rewardPoints = 20;
                break;
            case 28:
                rewardPoints = 40;
                break;
        }
        //保存积分 发消息到mq
//        mqHelper.send(
//                MqConstants.Exchange.LEARNING_EXCHANGE,
//                MqConstants.Key.SIGN_IN,
//                SignInMessage.of(userId, rewardPoints + 1)
//        );
        //封装vo
        SignResultVO vo = new SignResultVO();
        vo.setSignDays(days);
        vo.setRewardPoints(rewardPoints);
        return vo;
    }

    private int countSignDays(String key, int dayOfMonth) {
        //求本月签到的数量 bitFiled为十进制
        List<Long> bitField = redisTemplate.opsForValue().bitField(key, BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0));
        if (CollUtils.isEmpty(bitField)) {
            return 0;
        }
        Long num = bitField.get(0);
        int count = 0;
        while ((num & 1) == 1) {
            count++;
            num >>>= 1;
        }
        return count;
    }
}
