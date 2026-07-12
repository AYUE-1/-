package com.petadoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.entity.SystemReport;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.Result;
import com.petadoption.mapper.SystemReportMapper;
import com.petadoption.mapper.UserMapper;
import com.petadoption.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final SystemReportMapper reportMapper;
    private final UserMapper userMapper;

    @PostMapping
    public Result<String> create(Authentication auth, @RequestBody Map<String, String> body) {
        Long userId = (Long) auth.getPrincipal();
        SystemReport report = new SystemReport();
        report.setReporterId(userId);
        report.setTargetType(body.get("targetType"));
        report.setTargetId(Long.valueOf(body.get("targetId")));
        report.setReason(body.get("reason"));
        report.setStatus("PENDING");
        reportMapper.insert(report);
        return Result.success("举报已提交");
    }

    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        LambdaQueryWrapper<SystemReport> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(SystemReport::getStatus, status);
        wrapper.orderByDesc(SystemReport::getCreatedAt);

        Page<SystemReport> pageObj = new Page<>(page, size);
        reportMapper.selectPage(pageObj, wrapper);

        List<Map<String, Object>> records = pageObj.getRecords().stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("reporterId", r.getReporterId());
            m.put("targetType", r.getTargetType());
            m.put("targetId", r.getTargetId());
            m.put("reason", r.getReason());
            m.put("status", r.getStatus());
            m.put("handlerNote", r.getHandlerNote());
            m.put("createdAt", r.getCreatedAt());
            User reporter = userMapper.selectById(r.getReporterId());
            m.put("reporterName", reporter != null ? reporter.getNickname() : "");
            return m;
        }).collect(Collectors.toList());

        return Result.success(Map.of("records", records, "total", pageObj.getTotal(), "page", page, "size", size));
    }

    @PutMapping("/admin/{id}/handle")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> handle(@PathVariable Long id, Authentication auth,
                                @RequestBody Map<String, String> body) {
        Long adminId = (Long) auth.getPrincipal();
        SystemReport report = reportMapper.selectById(id);
        if (report == null) throw new BusinessException("举报不存在");
        report.setStatus(body.get("status"));
        report.setHandlerNote(body.get("handlerNote"));
        report.setHandledBy(adminId);
        report.setHandledAt(LocalDateTime.now());
        reportMapper.updateById(report);
        return Result.success();
    }
}
