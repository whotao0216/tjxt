package com.tianji.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.learning.domain.po.PointsBoard;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lyh
 * @description 针对表【points_board(学霸天梯榜)】的数据库操作Mapper
 * @createDate 2024-04-12 18:48:19
 * @Entity com.tianji.learning.domain.po.PointsBoard
 */
@Mapper
public interface PointsBoardMapper extends BaseMapper<PointsBoard> {

}




