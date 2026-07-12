package com.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("t_assessment_question")
public class AssessmentQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String questionText;
    private String questionType;
    private String dimension;
    private String options;
    private String petCategory;
    private Double weight;
    private Integer sortOrder;
}
