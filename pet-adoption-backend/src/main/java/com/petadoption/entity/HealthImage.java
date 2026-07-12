package com.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("t_health_image")
public class HealthImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recordId;
    private String url;
    private Integer sortOrder;
}
