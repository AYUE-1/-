package com.petadoption.dto;

import lombok.Data;

@Data
public class PetQueryDTO {
    private Integer page = 1;
    private Integer size = 12;
    private String keyword;
    private Long categoryId;
    private String gender;
    private String petSize;
    private String status;
    private String sortBy;
}
