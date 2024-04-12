package com.tianji.remark.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.remark.domain.po.LikedRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lyh
 * @description 针对表【liked_record(点赞记录表)】的数据库操作Mapper
 * @createDate 2024-04-10 20:37:44
 * @Entity com.tianji.remark.domain.po.LikedRecord
 */
@Mapper
public interface LikedRecordMapper extends BaseMapper<LikedRecord> {

}




