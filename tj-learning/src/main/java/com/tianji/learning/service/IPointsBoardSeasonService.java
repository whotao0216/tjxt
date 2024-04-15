package com.tianji.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.learning.domain.po.PointsBoardSeason;
import com.tianji.learning.domain.vo.PointsBoardSeasonVO;

import java.util.List;

/**
 * @author lyh
 * @description 针对表【points_board_season】的数据库操作Service
 * @createDate 2024-04-12 18:48:19
 */
public interface IPointsBoardSeasonService extends IService<PointsBoardSeason> {

    List<PointsBoardSeasonVO> querySeasons();

    void createPointsBoardLatestTable(Integer id);
}
