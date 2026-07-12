package com.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_review")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reviewerId;
    private Long targetId;
    private Long adoptionId;
    private String roleType;
    private Integer rating;
    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
