package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tianji.learning.enums.QuestionStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 互动提问的问题表
 *
 * @TableName interaction_question
 */
@TableName(value = "interaction_question")
@Data
public class InteractionQuestion implements Serializable {
    /**
     * 主键，互动问题的id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 互动问题的标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 问题描述信息
     */
    @TableField(value = "description")
    private String description;

    /**
     * 所属课程id
     */
    @TableField(value = "course_id")
    private Long courseId;

    /**
     * 所属课程章id
     */
    @TableField(value = "chapter_id")
    private Long chapterId;

    /**
     * 所属课程节id
     */
    @TableField(value = "section_id")
    private Long sectionId;

    /**
     * 提问学员id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 最新的一个回答的id
     */
    @TableField(value = "latest_answer_id")
    private Long latestAnswerId;

    /**
     * 问题下的回答数量
     */
    @TableField(value = "answer_times")
    private Integer answerTimes;

    /**
     * 是否匿名，默认false
     */
    @TableField(value = "anonymity")
    private Boolean anonymity;

    /**
     * 是否被隐藏，默认false
     */
    @TableField(value = "hidden")
    private Boolean hidden;

    /**
     * 管理端问题状态：0-未查看，1-已查看
     */
    @TableField(value = "status")
    private QuestionStatus status;

    /**
     * 提问时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

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
        InteractionQuestion other = (InteractionQuestion) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
                && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
                && (this.getCourseId() == null ? other.getCourseId() == null : this.getCourseId().equals(other.getCourseId()))
                && (this.getChapterId() == null ? other.getChapterId() == null : this.getChapterId().equals(other.getChapterId()))
                && (this.getSectionId() == null ? other.getSectionId() == null : this.getSectionId().equals(other.getSectionId()))
                && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
                && (this.getLatestAnswerId() == null ? other.getLatestAnswerId() == null : this.getLatestAnswerId().equals(other.getLatestAnswerId()))
                && (this.getAnswerTimes() == null ? other.getAnswerTimes() == null : this.getAnswerTimes().equals(other.getAnswerTimes()))
                && (this.getAnonymity() == null ? other.getAnonymity() == null : this.getAnonymity().equals(other.getAnonymity()))
                && (this.getHidden() == null ? other.getHidden() == null : this.getHidden().equals(other.getHidden()))
                && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getCourseId() == null) ? 0 : getCourseId().hashCode());
        result = prime * result + ((getChapterId() == null) ? 0 : getChapterId().hashCode());
        result = prime * result + ((getSectionId() == null) ? 0 : getSectionId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getLatestAnswerId() == null) ? 0 : getLatestAnswerId().hashCode());
        result = prime * result + ((getAnswerTimes() == null) ? 0 : getAnswerTimes().hashCode());
        result = prime * result + ((getAnonymity() == null) ? 0 : getAnonymity().hashCode());
        result = prime * result + ((getHidden() == null) ? 0 : getHidden().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", title=").append(title);
        sb.append(", description=").append(description);
        sb.append(", courseId=").append(courseId);
        sb.append(", chapterId=").append(chapterId);
        sb.append(", sectionId=").append(sectionId);
        sb.append(", userId=").append(userId);
        sb.append(", latestAnswerId=").append(latestAnswerId);
        sb.append(", answerTimes=").append(answerTimes);
        sb.append(", anonymity=").append(anonymity);
        sb.append(", hidden=").append(hidden);
        sb.append(", status=").append(status);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}