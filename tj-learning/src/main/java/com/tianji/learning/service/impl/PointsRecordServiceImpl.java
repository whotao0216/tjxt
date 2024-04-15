package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.DateUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.constants.RedisConstants;
import com.tianji.learning.domain.po.PointsRecord;
import com.tianji.learning.domain.vo.PointsStatisticsVO;
import com.tianji.learning.enums.PointsRecordType;
import com.tianji.learning.mapper.PointsRecordMapper;
import com.tianji.learning.mq.msg.SignInMessage;
import com.tianji.learning.service.IPointsRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lyh
 * @description 针对表【points_record(学习积分记录，每个月底清零)】的数据库操作Service实现
 * @createDate 2024-04-12 18:48:19
 */
@Service
@RequiredArgsConstructor
public class PointsRecordServiceImpl extends ServiceImpl<PointsRecordMapper, PointsRecord>
        implements IPointsRecordService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void addPointRecord(SignInMessage msg, PointsRecordType type) {
        if (msg.getUserId() == null || msg.getPoints() == null) {
            return;
        }
        int realPoints = msg.getPoints();
        int maxPoints = type.getMaxPoints();
        if (maxPoints > 0) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime dayStartTime = DateUtils.getDayStartTime(now);
            LocalDateTime dayEndTime = DateUtils.getDayEndTime(now);
            QueryWrapper<PointsRecord> wrapper = new QueryWrapper<>();
            wrapper.select("sum(points) as points");
            wrapper.eq("points_record_type", type);
            wrapper.eq("user_id", msg.getUserId());
            wrapper.between("timestamp", dayStartTime, dayEndTime);
            Map<String, Object> map = getMap(wrapper);
            int currentPoints = 0;//已获得积分
            if (map != null) {
                BigDecimal points = (BigDecimal) map.get("points");
                currentPoints = points.intValue();
            }
            if (currentPoints >= maxPoints) {
                return;
            }
            //计算实际增加分数
            if (currentPoints + realPoints >= maxPoints) {
                realPoints = maxPoints - currentPoints;
            }
        }
        PointsRecord pointsRecord = new PointsRecord();
        pointsRecord.setUserId(msg.getUserId());
        pointsRecord.setType(type);
        pointsRecord.setPoints(realPoints);
        save(pointsRecord);

        //累加积分保存到Redis 采用zset结构 当前赛季排行榜
        LocalDate now = LocalDate.now();
        String format = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = RedisConstants.POINTS_BOARD_KEY_PREFIX + format;
        redisTemplate.opsForZSet().incrementScore(key, msg.getUserId().toString(), realPoints);
    }

    @Override
    public List<PointsStatisticsVO> queryMyTodayPoints() {
        //获取当前用户
        Long userId = UserContext.getUser();
        //积分表获取数据，type分组
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dayStartTime = DateUtils.getDayStartTime(now);
        LocalDateTime dayEndTime = DateUtils.getDayEndTime(now);
        QueryWrapper<PointsRecord> wrapper = new QueryWrapper<>();
        wrapper.select("type,sum(points) as points");
        wrapper.eq("user_id", userId);
        wrapper.between("create_time", dayStartTime, dayEndTime);
        wrapper.groupBy("type");
        List<PointsRecord> list = this.list(wrapper);
        if (CollUtils.isEmpty(list)) {
            return CollUtils.emptyList();
        }
        //封装vo
        List<PointsStatisticsVO> vos = new ArrayList<>();
        for (PointsRecord pointsRecord : list) {
            PointsStatisticsVO vo = new PointsStatisticsVO();
            vo.setPoints(pointsRecord.getPoints());
            vo.setType(pointsRecord.getType().getDesc());
            vo.setMaxPoints(pointsRecord.getType().getMaxPoints());
            vos.add(vo);
        }
        return vos;
    }


}




