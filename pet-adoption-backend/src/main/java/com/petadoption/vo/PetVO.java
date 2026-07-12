package com.petadoption.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PetVO {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private String breed;
    private String age;
    private String gender;
    private String size;
    private String color;
    private String healthStatus;
    private String vaccination;
    private Integer sterilization;
    private String description;
    private String coverImage;
    private String status;
    private Integer viewCount;
    private Long createdBy;
    private LocalDateTime createdAt;
}
