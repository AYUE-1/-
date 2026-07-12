package com.petadoption.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class AssessmentSubmitDTO {
    private String petCategory;  // CAT, DOG, ALL
    @NotEmpty(message = "答案不能为空")
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        private Long questionId;
        private Integer selectedIndex;  // index of selected option
    }
}
