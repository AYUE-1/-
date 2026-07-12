package com.petadoption.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RescuePostVO {
    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private String addressDesc;
    private String animalType;
    private String animalDesc;
    private String emergencyLevel;
    private String status;
    private String statusDesc;
    private Long claimedBy;
    private String claimedByName;
    private LocalDateTime claimedAt;
    private LocalDateTime rescuedAt;
    private String followUpText;
    private Integer viewCount;
    private Double distance;  // km from query point
    private List<String> images;
    private LocalDateTime createdAt;
}
