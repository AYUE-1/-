package com.petadoption.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdoptionApplyDTO {
    private Long petId;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "身份证号不能为空")
    private String idCard;

    @NotBlank(message = "联系电话不能为空")
    private String phone;

    @NotBlank(message = "居住地址不能为空")
    private String address;

    private String housingType;
    private String hasExperience;
    private String familyAgreed;
    private String occupation;
    private String incomeRange;
    private String reason;
}
