package com.petadoption.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RescueDetailVO extends RescuePostVO {
    private String contactPhone;  // only visible to claimer/poster
}
