package com.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_blacklist")
public class Blacklist {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reporterId;
    private Long targetId;
    private String reason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
