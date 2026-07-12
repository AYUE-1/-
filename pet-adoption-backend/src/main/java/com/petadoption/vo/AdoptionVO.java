package com.petadoption.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdoptionVO {
    private Long id;
    private Long petId;
    private String petName;
    private String petCoverImage;
    private Long userId;
    private String username;
    private String realName;
    private String idCard;
    private String phone;
    private String address;
    private String housingType;
    private Integer hasExperience;
    private Integer familyAgreed;
    private String occupation;
    private String incomeRange;
    private String reason;
    private String status;
    private String reviewComment;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private String ownerReviewComment;
    private Long ownerReviewedBy;
    private LocalDateTime ownerReviewedAt;
    private LocalDateTime createdAt;
}
