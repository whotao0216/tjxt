package com.tianji.remark.controller;

import com.tianji.remark.domain.dto.LikeRecordFormDTO;
import com.tianji.remark.service.ILikedRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Api(tags = "点赞相关功能")
@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikedRecordController {

    private final ILikedRecordService recordService;

    @ApiOperation("点赞或取消赞")
    @PostMapping
    public void addLikedRecord(@RequestBody @Validated LikeRecordFormDTO dto) {
        recordService.addLikedRecord(dto);
    }

    @GetMapping("/list")
    @ApiOperation("查询点赞")
    public Set<Long> getLikedStatusByBizIds(@RequestParam List<Long> bizIds) {
        return recordService.getLikedStatusByBizIds(bizIds);
    }
}
