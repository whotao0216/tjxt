package com.tianji.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.learning.domain.po.PointsRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lyh
 * @description 针对表【points_record(学习积分记录，每个月底清零)】的数据库操作Mapper
 * @createDate 2024-04-12 18:48:19
 * @Entity com.tianji.learning.domain.po.PointsRecord
 */
@Mapper
public interface PointsRecordMapper extends BaseMapper<PointsRecord> {

}




