package com.petadoption.controller;

import com.petadoption.entity.HealthRecord;
import com.petadoption.exception.Result;
import com.petadoption.service.HealthRecordService;
import com.petadoption.vo.HealthRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthRecordController {

    private final HealthRecordService healthRecordService;

    @GetMapping("/pets/{petId}/records")
    public Result<List<HealthRecordVO>> getRecords(@PathVariable Long petId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(healthRecordService.getRecords(petId, userId));
    }

    @PostMapping("/records")
    public Result<HealthRecordVO> create(@RequestBody HealthRecord record, Authentication auth,
                                          @RequestParam(required = false) List<String> images) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(healthRecordService.create(record.getPetId(), userId, record, images));
    }

    @PutMapping("/records/{id}")
    public Result<HealthRecordVO> update(@PathVariable Long id, Authentication auth,
                                          @RequestBody HealthRecord record) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(healthRecordService.update(id, userId, record));
    }

    @DeleteMapping("/records/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        healthRecordService.delete(id, userId);
        return Result.success();
    }

    @GetMapping("/reminders")
    public Result<List<HealthRecordVO>> getReminders(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(healthRecordService.getReminders(userId));
    }
}
