package com.petadoption.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PetDetailVO extends PetVO {
    private List<String> images;
    private Boolean isFavorited;
}
