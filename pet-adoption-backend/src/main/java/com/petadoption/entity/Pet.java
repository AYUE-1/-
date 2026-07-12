package com.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_pet")
public class Pet {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long categoryId;
    private String breed;
    private String age;
    private String gender;
    @TableField("pet_size")
    private String size;
    private String color;
    private String healthStatus;
    private String vaccination;
    private Integer sterilization;
    private String description;
    private String coverImage;
    private String status;
    private Integer viewCount;
    private String personality;
    private String location;
    private Integer isAdopted;
    private LocalDateTime adoptedAt;
    private Long adoptedBy;
    private String weight;
    private String source;
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
