package com.petadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.dto.AdoptionApplyDTO;
import com.petadoption.dto.AdoptionReviewDTO;
import com.petadoption.entity.Adoption;
import com.petadoption.entity.Pet;
import com.petadoption.entity.User;
import com.petadoption.enums.AdoptionStatusEnum;
import com.petadoption.exception.BusinessException;
import com.petadoption.mapper.AdoptionMapper;
import com.petadoption.mapper.PetMapper;
import com.petadoption.mapper.UserMapper;
import com.petadoption.service.AdoptionService;
import com.petadoption.vo.AdoptionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdoptionServiceImpl implements AdoptionService {

    private final AdoptionMapper adoptionMapper;
    private final PetMapper petMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AdoptionVO apply(Long userId, AdoptionApplyDTO dto) {
        Pet pet = petMapper.selectById(dto.getPetId());
        if (pet == null) {
            throw new BusinessException("宠物信息不存在");
        }
        if (!"AVAILABLE".equals(pet.getStatus())) {
            throw new BusinessException("该宠物当前不可领养");
        }

        // 检查是否已有进行中的申请（PENDING 或 ADMIN_APPROVED）
        Long count = adoptionMapper.selectCount(
                new LambdaQueryWrapper<Adoption>()
                        .eq(Adoption::getUserId, userId)
                        .eq(Adoption::getPetId, dto.getPetId())
                        .in(Adoption::getStatus, List.of("PENDING", "ADMIN_APPROVED"))
        );
        if (count > 0) {
            throw new BusinessException("您已提交过申请，请等待审核结果");
        }

        Adoption adoption = new Adoption();
        adoption.setPetId(dto.getPetId());
        adoption.setUserId(userId);
        adoption.setRealName(dto.getRealName());
        adoption.setIdCard(dto.getIdCard());
        adoption.setPhone(dto.getPhone());
        adoption.setAddress(dto.getAddress());
        adoption.setHousingType(dto.getHousingType());
        adoption.setHasExperience(convertToInt(dto.getHasExperience()));
        adoption.setFamilyAgreed(convertToInt(dto.getFamilyAgreed()));
        adoption.setOccupation(dto.getOccupation());
        adoption.setIncomeRange(dto.getIncomeRange());
        adoption.setReason(dto.getReason());
        adoption.setStatus("PENDING");
        adoptionMapper.insert(adoption);

        return toAdoptionVO(adoption);
    }

    @Override
    public Page<AdoptionVO> getMyApplications(Long userId, int page, int size) {
        Page<Adoption> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Adoption> wrapper = new LambdaQueryWrapper<Adoption>()
                .eq(Adoption::getUserId, userId)
                .orderByDesc(Adoption::getCreatedAt);
        adoptionMapper.selectPage(pageObj, wrapper);

        Page<AdoptionVO> resultPage = new Page<>(page, size, pageObj.getTotal());
        resultPage.setRecords(pageObj.getRecords().stream().map(this::toAdoptionVO).toList());
        return resultPage;
    }

    @Override
    public AdoptionVO getDetail(Long id) {
        Adoption adoption = adoptionMapper.selectById(id);
        if (adoption == null) {
            throw new BusinessException("申请记录不存在");
        }
        return toAdoptionVO(adoption);
    }

    @Override
    @Transactional
    public void cancel(Long id, Long userId) {
        Adoption adoption = adoptionMapper.selectById(id);
        if (adoption == null) {
            throw new BusinessException("申请记录不存在");
        }
        if (!adoption.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此申请");
        }
        String oldStatus = adoption.getStatus();
        if (!"PENDING".equals(oldStatus) && !"ADMIN_APPROVED".equals(oldStatus) && !"APPROVED".equals(oldStatus)) {
            throw new BusinessException("当前状态不允许取消");
        }
        adoption.setStatus("CANCELLED");
        adoptionMapper.updateById(adoption);

        // 取消已通过的申请时，将宠物状态还原为可领养
        if ("APPROVED".equals(oldStatus)) {
            Pet pet = petMapper.selectById(adoption.getPetId());
            if (pet != null && "RESERVED".equals(pet.getStatus())) {
                pet.setStatus("AVAILABLE");
                petMapper.updateById(pet);
            }
        }
    }

    @Override
    public Page<AdoptionVO> getAdminList(int page, int size, String status) {
        Page<Adoption> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Adoption> wrapper = new LambdaQueryWrapper<Adoption>()
                .orderByDesc(Adoption::getCreatedAt);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Adoption::getStatus, status);
        }
        adoptionMapper.selectPage(pageObj, wrapper);

        Page<AdoptionVO> resultPage = new Page<>(page, size, pageObj.getTotal());
        resultPage.setRecords(pageObj.getRecords().stream().map(this::toAdoptionVO).toList());
        return resultPage;
    }

    @Override
    @Transactional
    public void review(Long id, AdoptionReviewDTO dto, Long reviewerId) {
        Adoption adoption = adoptionMapper.selectById(id);
        if (adoption == null) {
            throw new BusinessException("申请记录不存在");
        }
        // 管理员只能审核 PENDING 状态的申请
        if (!AdoptionStatusEnum.isValidTransition(adoption.getStatus(), dto.getStatus())) {
            throw new BusinessException("不允许的状态变更: " + adoption.getStatus() + " → " + dto.getStatus());
        }

        adoption.setStatus(dto.getStatus());
        adoption.setReviewComment(dto.getReviewComment());
        adoption.setReviewedBy(reviewerId);
        adoption.setReviewedAt(LocalDateTime.now());
        adoptionMapper.updateById(adoption);

        // 管理员拒绝时无需操作宠物状态（宠物保持 AVAILABLE）
    }

    @Override
    @Transactional
    public void ownerReview(Long id, AdoptionReviewDTO dto, Long ownerId) {
        Adoption adoption = adoptionMapper.selectById(id);
        if (adoption == null) {
            throw new BusinessException("申请记录不存在");
        }
        // 发布者只能审核 ADMIN_APPROVED 状态的申请
        if (!AdoptionStatusEnum.isValidTransition(adoption.getStatus(), dto.getStatus())) {
            throw new BusinessException("不允许的状态变更: " + adoption.getStatus() + " → " + dto.getStatus());
        }

        // 验证是该宠物的发布者
        Pet pet = petMapper.selectById(adoption.getPetId());
        if (pet == null || pet.getCreatedBy() == null || !pet.getCreatedBy().equals(ownerId)) {
            throw new BusinessException("无权审核此申请——您不是该宠物的发布者");
        }

        adoption.setStatus(dto.getStatus());
        adoption.setOwnerReviewComment(dto.getReviewComment());
        adoption.setOwnerReviewedBy(ownerId);
        adoption.setOwnerReviewedAt(LocalDateTime.now());
        adoptionMapper.updateById(adoption);

        // 发布者通过 → 宠物状态改为 RESERVED
        if ("APPROVED".equals(dto.getStatus())) {
            pet.setStatus("RESERVED");
            petMapper.updateById(pet);
        }
        // 发布者拒绝 → 宠物保持 AVAILABLE（无需操作）
    }

    @Override
    public Page<AdoptionVO> getOwnerApplications(Long ownerId, int page, int size, String status) {
        // 先查该用户发布的所有宠物ID
        List<Pet> ownerPets = petMapper.selectList(
                new LambdaQueryWrapper<Pet>().eq(Pet::getCreatedBy, ownerId)
        );
        List<Long> petIds = ownerPets.stream().map(Pet::getId).toList();

        Page<Adoption> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Adoption> wrapper = new LambdaQueryWrapper<Adoption>()
                .orderByDesc(Adoption::getCreatedAt);

        if (petIds.isEmpty()) {
            // 没有发布过宠物，返回空结果
            wrapper.eq(Adoption::getId, -1L);
        } else {
            wrapper.in(Adoption::getPetId, petIds);
        }

        if (status != null && !status.isEmpty()) {
            wrapper.eq(Adoption::getStatus, status);
        }

        adoptionMapper.selectPage(pageObj, wrapper);

        Page<AdoptionVO> resultPage = new Page<>(page, size, pageObj.getTotal());
        resultPage.setRecords(pageObj.getRecords().stream().map(this::toAdoptionVO).toList());
        return resultPage;
    }

    private AdoptionVO toAdoptionVO(Adoption a) {
        AdoptionVO vo = new AdoptionVO();
        vo.setId(a.getId());
        vo.setPetId(a.getPetId());
        vo.setUserId(a.getUserId());
        vo.setRealName(a.getRealName());
        vo.setIdCard(a.getIdCard());
        vo.setPhone(a.getPhone());
        vo.setAddress(a.getAddress());
        vo.setHousingType(a.getHousingType());
        vo.setHasExperience(a.getHasExperience());
        vo.setFamilyAgreed(a.getFamilyAgreed());
        vo.setOccupation(a.getOccupation());
        vo.setIncomeRange(a.getIncomeRange());
        vo.setReason(a.getReason());
        vo.setStatus(a.getStatus());
        vo.setReviewComment(a.getReviewComment());
        vo.setReviewedBy(a.getReviewedBy());
        vo.setReviewedAt(a.getReviewedAt());
        vo.setOwnerReviewComment(a.getOwnerReviewComment());
        vo.setOwnerReviewedBy(a.getOwnerReviewedBy());
        vo.setOwnerReviewedAt(a.getOwnerReviewedAt());
        vo.setCreatedAt(a.getCreatedAt());

        if (a.getPetId() != null) {
            Pet pet = petMapper.selectById(a.getPetId());
            if (pet != null) {
                vo.setPetName(pet.getName());
                vo.setPetCoverImage(pet.getCoverImage());
            }
        }

        if (a.getUserId() != null) {
            User user = userMapper.selectById(a.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }
        }

        return vo;
    }

    private Integer convertToInt(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        return ("有".equals(value) || "是".equals(value)) ? 1 : 0;
    }
}
