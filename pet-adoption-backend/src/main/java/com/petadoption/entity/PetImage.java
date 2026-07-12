package com.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("t_pet_image")
public class PetImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long petId;
    private String url;
    private Integer sortOrder;
}
