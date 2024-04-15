package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.common.utils.BeanUtils;
import com.tianji.learning.constants.LearningConstants;
import com.tianji.learning.domain.po.PointsBoardSeason;
import com.tianji.learning.domain.vo.PointsBoardSeasonVO;
import com.tianji.learning.mapper.PointsBoardSeasonMapper;
import com.tianji.learning.service.IPointsBoardSeasonService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lyh
 * @description 针对表【points_board_season】的数据库操作Service实现
 * @createDate 2024-04-12 18:48:19
 */
@Service
public class PointsBoardSeasonServiceImpl extends ServiceImpl<PointsBoardSeasonMapper, PointsBoardSeason>
        implements IPointsBoardSeasonService {

    @Override
    public List<PointsBoardSeasonVO> querySeasons() {

        List<PointsBoardSeason> list = this.lambdaQuery().list();
        return list.stream().map(pointsBoardSeason -> {
            return BeanUtils.copyBean(pointsBoardSeason, PointsBoardSeasonVO.class);
        }).collect(Collectors.toList());
    }

    /**
     * 创建赛季表
     *
     * @param id
     */
    @Override
    public void createPointsBoardLatestTable(Integer id) {
        getBaseMapper().createPointsBoardTable(LearningConstants.POINTS_BOARD_TABLE_PREFIX + id);
    }
}




