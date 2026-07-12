package com.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_health_record")
public class HealthRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long petId;
    private Long userId;
    private String recordType;
    private String title;
    private String description;
    private LocalDate recordDate;
    private LocalDate nextDate;
    private Integer reminderEnabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
