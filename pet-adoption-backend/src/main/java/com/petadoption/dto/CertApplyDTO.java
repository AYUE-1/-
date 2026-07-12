package com.petadoption.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CertApplyDTO {
    @NotBlank(message = "机构名称不能为空")
    private String shelterName;
    @NotBlank(message = "机构地址不能为空")
    private String shelterAddress;
    private String contactPerson;
    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;
    private String licenseInfo;
}
