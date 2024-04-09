package com.tianji.learning.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 互动问题的回答或评论
 *
 * @TableName interaction_reply
 */
@TableName(value = "interaction_reply")
@Data
public class InteractionReply implements Serializable {
    /**
     * 互动问题的回答id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 互动问题问题id
     */
    @TableField(value = "question_id")
    private Long questionId;

    /**
     * 回复的上级回答id
     */
    @TableField(value = "answer_id")
    private Long answerId;

    /**
     * 回答者id
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 回答内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 回复的目标用户id
     */
    @TableField(value = "target_user_id")
    private Long targetUserId;

    /**
     * 回复的目标回复id
     */
    @TableField(value = "target_reply_id")
    private Long targetReplyId;

    /**
     * 评论数量
     */
    @TableField(value = "reply_times")
    private Integer replyTimes;

    /**
     * 点赞数量
     */
    @TableField(value = "liked_times")
    private Integer likedTimes;

    /**
     * 是否被隐藏，默认false
     */
    @TableField(value = "hidden")
    private Boolean hidden;

    /**
     * 是否匿名，默认false
     */
    @TableField(value = "anonymity")
    private Boolean anonymity;

    /**
     * 创建时间
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
        InteractionReply other = (InteractionReply) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getQuestionId() == null ? other.getQuestionId() == null : this.getQuestionId().equals(other.getQuestionId()))
                && (this.getAnswerId() == null ? other.getAnswerId() == null : this.getAnswerId().equals(other.getAnswerId()))
                && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
                && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
                && (this.getTargetUserId() == null ? other.getTargetUserId() == null : this.getTargetUserId().equals(other.getTargetUserId()))
                && (this.getTargetReplyId() == null ? other.getTargetReplyId() == null : this.getTargetReplyId().equals(other.getTargetReplyId()))
                && (this.getReplyTimes() == null ? other.getReplyTimes() == null : this.getReplyTimes().equals(other.getReplyTimes()))
                && (this.getLikedTimes() == null ? other.getLikedTimes() == null : this.getLikedTimes().equals(other.getLikedTimes()))
                && (this.getHidden() == null ? other.getHidden() == null : this.getHidden().equals(other.getHidden()))
                && (this.getAnonymity() == null ? other.getAnonymity() == null : this.getAnonymity().equals(other.getAnonymity()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getQuestionId() == null) ? 0 : getQuestionId().hashCode());
        result = prime * result + ((getAnswerId() == null) ? 0 : getAnswerId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getTargetUserId() == null) ? 0 : getTargetUserId().hashCode());
        result = prime * result + ((getTargetReplyId() == null) ? 0 : getTargetReplyId().hashCode());
        result = prime * result + ((getReplyTimes() == null) ? 0 : getReplyTimes().hashCode());
        result = prime * result + ((getLikedTimes() == null) ? 0 : getLikedTimes().hashCode());
        result = prime * result + ((getHidden() == null) ? 0 : getHidden().hashCode());
        result = prime * result + ((getAnonymity() == null) ? 0 : getAnonymity().hashCode());
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
        sb.append(", questionId=").append(questionId);
        sb.append(", answerId=").append(answerId);
        sb.append(", userId=").append(userId);
        sb.append(", content=").append(content);
        sb.append(", targetUserId=").append(targetUserId);
        sb.append(", targetReplyId=").append(targetReplyId);
        sb.append(", replyTimes=").append(replyTimes);
        sb.append(", likedTimes=").append(likedTimes);
        sb.append(", hidden=").append(hidden);
        sb.append(", anonymity=").append(anonymity);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}