package com.tianji.remark.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.remark.domain.dto.LikeRecordFormDTO;
import com.tianji.remark.domain.po.LikedRecord;

import java.util.List;
import java.util.Set;

/**
 * @author lyh
 * @description 针对表【liked_record(点赞记录表)】的数据库操作Service
 * @createDate 2024-04-10 20:37:44
 */
public interface ILikedRecordService extends IService<LikedRecord> {

    void addLikedRecord(LikeRecordFormDTO dto);

    Set<Long> getLikedStatusByBizIds(List<Long> bizIds);

    void readLikedTimesAndSendMessage(String bizType, int maxBizSize);
}
