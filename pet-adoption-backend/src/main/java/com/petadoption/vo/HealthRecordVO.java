package com.petadoption.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class HealthRecordVO {
    private Long id;
    private Long petId;
    private String petName;
    private Long userId;
    private String recordType;
    private String recordTypeName;
    private String title;
    private String description;
    private LocalDate recordDate;
    private LocalDate nextDate;
    private Integer reminderEnabled;
    private List<String> images;
    private LocalDateTime createdAt;
}
