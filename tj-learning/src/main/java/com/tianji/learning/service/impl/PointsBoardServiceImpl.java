package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.learning.domain.po.PointsBoard;
import com.tianji.learning.mapper.PointsBoardMapper;
import com.tianji.learning.service.IPointsBoardService;
import org.springframework.stereotype.Service;

/**
 * @author lyh
 * @description 针对表【points_board(学霸天梯榜)】的数据库操作Service实现
 * @createDate 2024-04-12 18:48:19
 */
@Service
public class PointsBoardServiceImpl extends ServiceImpl<PointsBoardMapper, PointsBoard>
        implements IPointsBoardService {

}




