package com.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_volunteer")
public class Volunteer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String skills;
    private String availableTime;
    private String region;
    private String status;
    private String welfareActivities;
    private LocalDateTime approvedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
