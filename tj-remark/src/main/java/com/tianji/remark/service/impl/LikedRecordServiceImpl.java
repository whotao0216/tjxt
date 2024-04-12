package com.tianji.remark.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.remark.constants.RedisConstants;
import com.tianji.remark.domain.dto.LikeRecordFormDTO;
import com.tianji.remark.domain.dto.LikedTimesDTO;
import com.tianji.remark.domain.po.LikedRecord;
import com.tianji.remark.mapper.LikedRecordMapper;
import com.tianji.remark.service.ILikedRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author lyh
 * @description 针对表【liked_record(点赞记录表)】的数据库操作Service实现
 * @createDate 2024-04-10 20:37:44
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LikedRecordServiceImpl extends ServiceImpl<LikedRecordMapper, LikedRecord>
        implements ILikedRecordService {

    private final RabbitMqHelper rabbitMqHelper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void addLikedRecord(LikeRecordFormDTO dto) {
        //get current user
        Long userId = UserContext.getUser();
        //判断dto的liked
        boolean flag = dto.getLiked() ? liked(dto, userId) : unliked(dto, userId);
        //统计业务id总点赞数,depend on redis
//        Integer count = lambdaQuery()
//                .eq(LikedRecord::getBizId, dto.getBizId())
//                .count();
        String key = RedisConstants.LIKE_BIZ_KEY_PREFIX + dto.getBizId();
        Long count = redisTemplate.opsForSet().size(key);
        if (count == null) return;
        //发送消息到mq
       /* LikedTimesDTO likedTimesDTO = new LikedTimesDTO();
        likedTimesDTO.setBizId(dto.getBizId());
        likedTimesDTO.setLikedTimes(count);
        rabbitMqHelper.send(
                MqConstants.Exchange.LIKE_RECORD_EXCHANGE,
                StringUtils.format(MqConstants.Key.LIKED_TIMES_KEY_TEMPLATE, dto.getBizId()),
                likedTimesDTO);*/

        //用zset缓存点赞总数
        String bizTypeTotalLikeKey = RedisConstants.LIKES_TIMES_KEY_PREFIX + dto.getBizType();
        redisTemplate.opsForSet().add(bizTypeTotalLikeKey, dto.getBizId().toString(), count.toString());
    }

    @Override
    public Set<Long> getLikedStatusByBizIds(List<Long> bizIds) {
        /*Long userId = UserContext.getUser();
        List<LikedRecord> list = lambdaQuery()
                .eq(LikedRecord::getUserId, userId)
                .in(LikedRecord::getBizId, bizIds)
                .list();
        return list.stream().map(LikedRecord::getBizId).collect(Collectors.toSet());*/
        Long userId = UserContext.getUser();
        if (userId == null) return CollUtils.emptySet();
        Set<Long> likedBizIds = new HashSet<>();
        bizIds.forEach(bizId -> {
            Boolean member = redisTemplate.opsForSet().isMember(
                    RedisConstants.LIKES_TIMES_KEY_PREFIX + bizId,
                    userId.toString()
            );
            if (member) {
                likedBizIds.add(bizId);
            }
        });
        return likedBizIds;
    }

    @Override
    public void readLikedTimesAndSendMessage(String bizType, int maxBizSize) {
        List<LikedTimesDTO> list = new ArrayList<>();
        //拼接key
        String key = RedisConstants.LIKE_BIZ_KEY_PREFIX + bizType;
        //从Redis的zset中取出maxBizSize的业务信息
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().popMin(key, maxBizSize);
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            Double likedTimes = typedTuple.getScore();
            String bizId = typedTuple.getValue();
            if (StringUtils.isBlank(bizId) || likedTimes == null) {
                continue;
            }
            //封装dto
            LikedTimesDTO msg = LikedTimesDTO.of(Long.valueOf(bizId), likedTimes.intValue());
            list.add(msg);
        }

        //send to mq
        if (CollUtils.isNotEmpty(list)) {
            log.debug("批量发送点赞消息:{}", list);
            rabbitMqHelper.send(
                    MqConstants.Exchange.LIKE_RECORD_EXCHANGE,
                    StringUtils.format(MqConstants.Key.LIKED_TIMES_KEY_TEMPLATE, bizType),
                    list);
        }
    }

    private boolean unliked(LikeRecordFormDTO dto, Long userId) {
//        LikedRecord record = lambdaQuery()
//                .eq(LikedRecord::getUserId, userId)
//                .eq(LikedRecord::getBizId, dto.getBizId())
//                .one();
//        if (record == null) {
//            return false;
//        }
//        boolean result = removeById(record.getId());
//        return result;
        String key = RedisConstants.LIKE_BIZ_KEY_PREFIX + dto.getBizId();
        Long result = redisTemplate.opsForSet().remove(key, userId.toString());
        return result != null && result > 0;
    }

    private boolean liked(LikeRecordFormDTO dto, Long userId) {
//        LikedRecord record = lambdaQuery()
//                .eq(LikedRecord::getUserId, userId)
//                .eq(LikedRecord::getBizId, dto.getBizId())
//                .one();
//        if (record != null) {
//            //点过赞
//            return false;
//        }
//        LikedRecord likedRecord = BeanUtils.copyBean(dto, LikedRecord.class);
//        likedRecord.setUserId(userId);
//        boolean result = save(likedRecord);
//        return result;

        //基于Redis点赞业务
        //拼接key
        String key = RedisConstants.LIKE_BIZ_KEY_PREFIX + dto.getBizId();
        Long result = redisTemplate.opsForSet().add(key, userId.toString());
        return result != null && result > 0;
    }
}




