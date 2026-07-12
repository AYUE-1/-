package com.petadoption.dto;

import lombok.Data;

@Data
public class RescueQueryDTO {
    private int page = 1;
    private int size = 12;
    private String animalType;
    private String emergencyLevel;
    private String status;
    private Double latitude;
    private Double longitude;
    private Double radius;  // km
    private String sortBy;  // distance, latest
}
