package com.tianji.learning.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.user.UserClient;
import com.tianji.api.dto.user.UserDTO;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.constants.RedisConstants;
import com.tianji.learning.domain.po.PointsBoard;
import com.tianji.learning.domain.query.PointsBoardQuery;
import com.tianji.learning.domain.vo.PointsBoardItemVO;
import com.tianji.learning.domain.vo.PointsBoardVO;
import com.tianji.learning.mapper.PointsBoardMapper;
import com.tianji.learning.service.IPointsBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lyh
 * @description 针对表【points_board(学霸天梯榜)】的数据库操作Service实现
 * @createDate 2024-04-12 18:48:19
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings(value = "all")
public class PointsBoardServiceImpl extends ServiceImpl<PointsBoardMapper, PointsBoard>
        implements IPointsBoardService {

    private final StringRedisTemplate redisTemplate;
    private final UserClient userClient;

    @Override
    public PointsBoardVO queryPointsBoard(PointsBoardQuery query) {
        Long userId = UserContext.getUser();
        //判断是查询当前还是历史赛季
        Long season = query.getSeason();
        boolean isCurrent = season == null || season == 0;
        LocalDate now = LocalDate.now();
        String format = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String key = RedisConstants.POINTS_BOARD_KEY_PREFIX + format;
        //查询我的排名和积分信息
        PointsBoard pointsBoard = isCurrent ? queryCurrentBoard(key) : queryHistoryBoard(key);
        //分页查询赛季列表
        List<PointsBoard> list = isCurrent ? queryCurrentSeason(key, query.getPageNo(), query.getPageSize()) : queryHistorySeason(key, query.getPageNo(), query.getPageSize());
        //调用用户服务获取用户信息封装用户ID集合
        Set<Long> uIds = list.stream().map(PointsBoard::getUserId).collect(Collectors.toSet());
        List<UserDTO> userDTOS = userClient.queryUserByIds(uIds);
        Map<Long, String> map = new HashMap<>(uIds.size());
        if (CollUtils.isEmpty(userDTOS)) {
            throw new BizIllegalException("用户不存在");
        }
        map = userDTOS.stream().collect(Collectors.toMap(UserDTO::getId, UserDTO::getName));

        PointsBoardVO vo = new PointsBoardVO();
        vo.setRank(pointsBoard.getRank());//排名
        vo.setPoints(pointsBoard.getPoints());//积分
        List<PointsBoardItemVO> itemVOList = new ArrayList<>();
        for (PointsBoard board : list) {
            itemVOList.add(
                    PointsBoardItemVO.builder()
                            .points(board.getPoints())
                            .rank(board.getRank())
                            .name(map.get(board.getUserId()))
                            .build()
            );
        }
        vo.setBoardList(itemVOList);
        return vo;
    }

    /**
     * db查询历史赛季列表
     *
     * @param key
     * @param pageNo
     * @param pageSize
     * @return
     */
    private List<PointsBoard> queryHistorySeason(String key, Integer pageNo, Integer pageSize) {
        return null;
    }

    /**
     * redis查询当前赛季列表
     *
     * @param key
     * @param pageNo
     * @param pageSize
     * @return
     */
    private List<PointsBoard> queryCurrentSeason(String key, Integer pageNo, Integer pageSize) {
        int start = (pageNo - 1) * pageSize;
        int end = pageNo * pageSize - 1;
        Set<ZSetOperations.TypedTuple<String>> typeTuples = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        int rank = 1 + start;
        List<PointsBoard> list = new ArrayList<>();
        if (CollUtils.isEmpty(typeTuples)) {
            return CollUtils.emptyList();
        }
        for (ZSetOperations.TypedTuple<String> typeTuple : typeTuples) {
            String value = typeTuple.getValue();
            Double score = typeTuple.getScore();
            list.add(PointsBoard.builder().points(score.intValue())
                    .points(score.intValue())
                    .userId(Long.valueOf(typeTuple.getValue()))
                    .rank(rank++).build());
        }
        return list;
    }

    //查询历史赛季我的积分和排名,db
    private PointsBoard queryHistoryBoard(String key) {
        return null;
    }

    //查询当前赛季我的积分和排名 redis
    private PointsBoard queryCurrentBoard(String key) {
        // 1.绑定key
        BoundZSetOperations<String, String> ops = redisTemplate.boundZSetOps(key);
        // 2.获取当前用户信息
        String userId = UserContext.getUser().toString();
        // 3.查询积分
        Double points = ops.score(userId);
        // 4.查询排名
        Long rank = ops.reverseRank(userId);
        // 5.封装返回
        PointsBoard p = new PointsBoard();
        p.setPoints(points == null ? 0 : points.intValue());
        p.setRank(rank == null ? 0 : rank.intValue() + 1);
        p.setUserId(Long.valueOf(userId));
        return p;
    }
}




