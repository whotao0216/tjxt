package com.tianji.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.learning.domain.po.LearningRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lyh
 * @description 针对表【learning_record(学习记录表)】的数据库操作Mapper
 * @createDate 2024-04-07 17:22:16
 * @Entity com.tianji.learning.domain.po.LearningRecord
 */
@Mapper
public interface LearningRecordMapper extends BaseMapper<LearningRecord> {

}




