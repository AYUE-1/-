package com.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_assessment")
public class Assessment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String answers;
    private String resultData;
    private String petCategory;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
