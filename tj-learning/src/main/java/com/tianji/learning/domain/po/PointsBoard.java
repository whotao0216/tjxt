package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 学霸天梯榜
 *
 * @TableName points_board
 */
@TableName(value = "points_board")
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
public class PointsBoard implements Serializable {
    /**
     * 榜单id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 学生id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 积分值
     */
    @TableField(value = "points")
    private Integer points;

    /**
     * 名次，只记录赛季前100
     */
    @TableField(value = "rank")
    private Integer rank;

    /**
     * 赛季，例如 1,就是第一赛季，2-就是第二赛季
     */
    @TableField(value = "season")
    private Integer season;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        PointsBoard other = (PointsBoard) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
                && (this.getPoints() == null ? other.getPoints() == null : this.getPoints().equals(other.getPoints()))
                && (this.getRank() == null ? other.getRank() == null : this.getRank().equals(other.getRank()))
                && (this.getSeason() == null ? other.getSeason() == null : this.getSeason().equals(other.getSeason()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getPoints() == null) ? 0 : getPoints().hashCode());
        result = prime * result + ((getRank() == null) ? 0 : getRank().hashCode());
        result = prime * result + ((getSeason() == null) ? 0 : getSeason().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", points=").append(points);
        sb.append(", rank=").append(rank);
        sb.append(", season=").append(season);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}