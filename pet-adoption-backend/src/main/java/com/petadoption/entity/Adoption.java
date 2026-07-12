package com.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_adoption")
public class Adoption {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long petId;
    private Long userId;
    private String realName;
    private String idCard;
    private String phone;
    private String address;
    private String housingType;
    private Integer hasExperience;
    private Integer familyAgreed;
    private String occupation;
    private String incomeRange;
    private String reason;
    private String status;
    private String reviewComment;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;

    /** 发布者（宠物主人）审核 */
    private String ownerReviewComment;
    private Long ownerReviewedBy;
    private LocalDateTime ownerReviewedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
