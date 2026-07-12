package com.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("t_rescue_image")
public class RescueImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long rescueId;
    private String url;
    private Integer sortOrder;
}
