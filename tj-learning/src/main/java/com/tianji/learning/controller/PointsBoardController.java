package com.tianji.learning.controller;

import com.tianji.learning.domain.query.PointsBoardQuery;
import com.tianji.learning.domain.vo.PointsBoardVO;
import com.tianji.learning.service.IPointsBoardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "排行榜接口")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/boards")
public class PointsBoardController {

    private final IPointsBoardService boardService;

    @ApiOperation("查询学霸天梯榜")
    @GetMapping
    public PointsBoardVO queryPointsBoard(PointsBoardQuery query) {
        return boardService.queryPointsBoard(query);
    }
}
