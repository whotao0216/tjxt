package com.tianji.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianji.learning.domain.po.PointsBoardSeason;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author lyh
 * @description 针对表【points_board_season】的数据库操作Mapper
 * @createDate 2024-04-12 18:48:19
 * @Entity com.tianji.learning.domain.po.PointsBoardSeason
 */
@Mapper
public interface PointsBoardSeasonMapper extends BaseMapper<PointsBoardSeason> {

    @Insert(value = "CREATE TABLE ${tableName}\n" +
            "(\n" +
            "    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '榜单id',\n" +
            "    user_id BIGINT NOT NULL COMMENT '学生id',\n" +
            "    points  INT    NOT NULL COMMENT '积分值',\n" +
            "    PRIMARY KEY (id) USING BTREE,\n" +
            "    INDEX `idx_user_id` (`user_id`) USING BTREE\n" +
            ")\n" +
            "    COMMENT ='学霸天梯榜'\n" +
            "    COLLATE = 'utf8mb4_0900_ai_ci'\n" +
            "    ENGINE = InnoDB\n" +
            "    ROW_FORMAT = DYNAMIC")
    void createPointsBoardTable(@Param("tableName") String tableName);
}




