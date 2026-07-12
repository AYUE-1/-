package com.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_rescue_post")
public class RescuePost {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private String addressDesc;
    private String animalType;
    private String animalDesc;
    private String emergencyLevel;
    private String status;
    private Long claimedBy;
    private LocalDateTime claimedAt;
    private LocalDateTime rescuedAt;
    private String followUpText;
    private Integer viewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
