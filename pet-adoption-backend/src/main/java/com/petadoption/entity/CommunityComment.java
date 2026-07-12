package com.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_community_comment")
public class CommunityComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private Long parentId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
