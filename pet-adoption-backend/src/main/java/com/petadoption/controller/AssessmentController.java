package com.petadoption.controller;

import com.petadoption.dto.AssessmentSubmitDTO;
import com.petadoption.entity.AssessmentQuestion;
import com.petadoption.exception.Result;
import com.petadoption.service.AssessmentService;
import com.petadoption.vo.AssessmentResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessment")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    @GetMapping("/questions")
    public Result<List<AssessmentQuestion>> getQuestions(@RequestParam(required = false) String category) {
        return Result.success(assessmentService.getQuestions(category));
    }

    @PostMapping("/submit")
    public Result<AssessmentResultVO> submit(Authentication auth, @Valid @RequestBody AssessmentSubmitDTO dto) {
        Long userId = auth != null ? (Long) auth.getPrincipal() : null;
        return Result.success(assessmentService.submitAssessment(userId, dto));
    }

    @GetMapping("/result/{id}")
    public Result<AssessmentResultVO> getResult(@PathVariable Long id) {
        return Result.success(assessmentService.getResult(id));
    }

    @GetMapping("/history")
    public Result<List<AssessmentResultVO>> getHistory(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(assessmentService.getHistory(userId));
    }
}
