package com.petadoption.service;

import com.petadoption.dto.AssessmentSubmitDTO;
import com.petadoption.entity.AssessmentQuestion;
import com.petadoption.vo.AssessmentResultVO;

import java.util.List;

public interface AssessmentService {
    List<AssessmentQuestion> getQuestions(String petCategory);
    AssessmentResultVO submitAssessment(Long userId, AssessmentSubmitDTO dto);
    AssessmentResultVO getResult(Long assessmentId);
    List<AssessmentResultVO> getHistory(Long userId);
}
