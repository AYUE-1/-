package com.petadoption.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AssessmentResultVO {
    private Long id;
    private Map<String, Double> dimensionScores;     // dimension -> score (0-1)
    private List<BreedRecommendation> recommendations; // top 5 breeds
    private List<String> careTips;                     // personalized care suggestions
    private ChecklistResult checklist;                 // pre-adoption readiness
    private Double overallScore;                       // 0-100 overall readiness

    @Data
    public static class BreedRecommendation {
        private String breedName;
        private String breedDesc;
        private Double matchScore;    // 0-100 match percentage
        private String suitableReason;
        private List<String> tags;    // e.g., ["亲人", "适合新手", "运动量大"]
    }

    @Data
    public static class ChecklistResult {
        private Boolean ready;           // overall ready or not
        private Integer score;           // 0-100
        private List<String> passedItems;
        private List<String> warningItems;
    }
}
