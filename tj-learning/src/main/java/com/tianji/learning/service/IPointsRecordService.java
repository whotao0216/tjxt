package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.learning.domain.po.PointsRecord;
import com.tianji.learning.domain.vo.PointsStatisticsVO;
import com.tianji.learning.enums.PointsRecordType;
import com.tianji.learning.mq.msg.SignInMessage;

import java.util.List;

/**
 * @author lyh
 * @description 针对表【points_record(学习积分记录，每个月底清零)】的数据库操作Service
 * @createDate 2024-04-12 18:48:19
 */
public interface IPointsRecordService extends IService<PointsRecord> {

    void addPointRecord(SignInMessage msg, PointsRecordType sign);

    List<PointsStatisticsVO> queryMyTodayPoints();

}
