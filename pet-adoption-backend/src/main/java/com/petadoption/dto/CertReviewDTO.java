package com.petadoption.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CertReviewDTO {
    @NotBlank(message = "审核结果不能为空")
    private String status;  // APPROVED or REJECTED
    private String comment;
}
