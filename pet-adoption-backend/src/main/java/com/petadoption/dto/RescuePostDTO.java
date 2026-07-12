package com.petadoption.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class RescuePostDTO {
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "描述不能为空")
    private String description;
    @NotNull(message = "纬度不能为空")
    private Double latitude;
    @NotNull(message = "经度不能为空")
    private Double longitude;
    private String addressDesc;
    @NotBlank(message = "动物类型不能为空")
    private String animalType;
    private String animalDesc;
    private String emergencyLevel;
    private List<String> images;
}
