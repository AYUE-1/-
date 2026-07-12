package com.petadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadoption.entity.*;
import com.petadoption.enums.HealthRecordTypeEnum;
import com.petadoption.exception.BusinessException;
import com.petadoption.mapper.*;
import com.petadoption.service.HealthRecordService;
import com.petadoption.vo.HealthRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HealthRecordServiceImpl implements HealthRecordService {

    private final HealthRecordMapper recordMapper;
    private final HealthImageMapper imageMapper;
    private final PetMapper petMapper;

    @Override
    public List<HealthRecordVO> getRecords(Long petId, Long userId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) throw new BusinessException("宠物不存在");
        if (!pet.getCreatedBy().equals(userId)) throw new BusinessException("无权查看");

        return recordMapper.selectList(
            new LambdaQueryWrapper<HealthRecord>()
                .eq(HealthRecord::getPetId, petId)
                .orderByDesc(HealthRecord::getRecordDate)
        ).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HealthRecordVO create(Long petId, Long userId, HealthRecord record, List<String> images) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) throw new BusinessException("宠物不存在");
        if (!pet.getCreatedBy().equals(userId)) throw new BusinessException("无权操作");

        record.setPetId(petId);
        record.setUserId(userId);
        recordMapper.insert(record);

        if (images != null) {
            for (int i = 0; i < images.size(); i++) {
                HealthImage img = new HealthImage();
                img.setRecordId(record.getId());
                img.setUrl(images.get(i));
                img.setSortOrder(i);
                imageMapper.insert(img);
            }
        }
        return toVO(record);
    }

    @Override
    public HealthRecordVO update(Long id, Long userId, HealthRecord updated) {
        HealthRecord record = recordMapper.selectById(id);
        if (record == null) throw new BusinessException("记录不存在");
        if (!record.getUserId().equals(userId)) throw new BusinessException("无权操作");
        record.setTitle(updated.getTitle());
        record.setDescription(updated.getDescription());
        record.setRecordType(updated.getRecordType());
        record.setRecordDate(updated.getRecordDate());
        record.setNextDate(updated.getNextDate());
        record.setReminderEnabled(updated.getReminderEnabled());
        recordMapper.updateById(record);
        return toVO(record);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        HealthRecord record = recordMapper.selectById(id);
        if (record == null) throw new BusinessException("记录不存在");
        if (!record.getUserId().equals(userId)) throw new BusinessException("无权操作");
        imageMapper.delete(new LambdaQueryWrapper<HealthImage>().eq(HealthImage::getRecordId, id));
        recordMapper.deleteById(id);
    }

    @Override
    public List<HealthRecordVO> getReminders(Long userId) {
        return recordMapper.selectList(
            new LambdaQueryWrapper<HealthRecord>()
                .eq(HealthRecord::getUserId, userId)
                .eq(HealthRecord::getReminderEnabled, 1)
                .le(HealthRecord::getNextDate, LocalDate.now().plusDays(7))
        ).stream().map(this::toVO).collect(Collectors.toList());
    }

    private HealthRecordVO toVO(HealthRecord record) {
        HealthRecordVO vo = new HealthRecordVO();
        vo.setId(record.getId());
        vo.setPetId(record.getPetId());
        vo.setUserId(record.getUserId());
        vo.setRecordType(record.getRecordType());
        vo.setRecordTypeName(HealthRecordTypeEnum.getDesc(record.getRecordType()));
        vo.setTitle(record.getTitle());
        vo.setDescription(record.getDescription());
        vo.setRecordDate(record.getRecordDate());
        vo.setNextDate(record.getNextDate());
        vo.setReminderEnabled(record.getReminderEnabled());
        vo.setCreatedAt(record.getCreatedAt());

        Pet pet = petMapper.selectById(record.getPetId());
        if (pet != null) vo.setPetName(pet.getName());

        List<HealthImage> images = imageMapper.selectList(
            new LambdaQueryWrapper<HealthImage>().eq(HealthImage::getRecordId, record.getId())
                .orderByAsc(HealthImage::getSortOrder));
        vo.setImages(images.stream().map(HealthImage::getUrl).collect(Collectors.toList()));
        return vo;
    }
}
