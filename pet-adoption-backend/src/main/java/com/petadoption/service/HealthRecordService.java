package com.petadoption.service;

import com.petadoption.entity.HealthRecord;
import com.petadoption.vo.HealthRecordVO;

import java.util.List;

public interface HealthRecordService {
    List<HealthRecordVO> getRecords(Long petId, Long userId);
    HealthRecordVO create(Long petId, Long userId, HealthRecord record, List<String> images);
    HealthRecordVO update(Long id, Long userId, HealthRecord record);
    void delete(Long id, Long userId);
    List<HealthRecordVO> getReminders(Long userId);
}
