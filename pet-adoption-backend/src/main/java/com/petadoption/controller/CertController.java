package com.petadoption.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.dto.CertApplyDTO;
import com.petadoption.dto.CertReviewDTO;
import com.petadoption.exception.Result;
import com.petadoption.service.CertService;
import com.petadoption.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CertController {

    private final CertService certService;

    @PostMapping("/api/users/cert")
    public Result<String> applyCert(Authentication auth, @Valid @RequestBody CertApplyDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        certService.applyCert(userId, dto.getShelterName(), dto.getShelterAddress(), dto.getLicenseInfo());
        return Result.success("认证申请已提交");
    }

    @GetMapping("/api/admin/certs")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<UserVO>> getCertList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return Result.success(certService.getCertList(page, size, status));
    }

    @PutMapping("/api/admin/certs/{userId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> reviewCert(@PathVariable Long userId, @Valid @RequestBody CertReviewDTO dto) {
        certService.reviewCert(userId, dto.getStatus(), dto.getComment());
        return Result.success("审核完成");
    }
}
