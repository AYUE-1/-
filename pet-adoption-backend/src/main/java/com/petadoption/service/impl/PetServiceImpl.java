package com.petadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.dto.PageResult;
import com.petadoption.dto.PetQueryDTO;
import com.petadoption.entity.Category;
import com.petadoption.entity.Pet;
import com.petadoption.entity.PetImage;
import com.petadoption.exception.BusinessException;
import com.petadoption.mapper.CategoryMapper;
import com.petadoption.mapper.PetImageMapper;
import com.petadoption.mapper.PetMapper;
import com.petadoption.service.PetService;
import com.petadoption.vo.PetDetailVO;
import com.petadoption.vo.PetVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private final PetMapper petMapper;
    private final PetImageMapper petImageMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public PageResult<PetVO> queryPets(PetQueryDTO dto) {
        Page<Pet> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();

        if (!StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(Pet::getStatus, "AVAILABLE");
        } else if (!"ALL".equals(dto.getStatus())) {
            wrapper.eq(Pet::getStatus, dto.getStatus());
        }
        // "ALL" = no status filter

        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like(Pet::getName, dto.getKeyword())
                    .or().like(Pet::getBreed, dto.getKeyword())
                    .or().like(Pet::getDescription, dto.getKeyword()));
        }

        if (dto.getCategoryId() != null) {
            wrapper.eq(Pet::getCategoryId, dto.getCategoryId());
        }

        if (StringUtils.hasText(dto.getGender())) {
            wrapper.eq(Pet::getGender, dto.getGender());
        }

        if (StringUtils.hasText(dto.getPetSize())) {
            wrapper.eq(Pet::getSize, dto.getPetSize());
        }

        if ("viewCount".equals(dto.getSortBy())) {
            wrapper.orderByDesc(Pet::getViewCount);
        } else {
            wrapper.orderByDesc(Pet::getCreatedAt);
        }

        petMapper.selectPage(page, wrapper);

        // 批量获取分类名称，避免 N+1 查询
        Map<Long, String> categoryNameMap = buildCategoryNameMap(page.getRecords());

        List<PetVO> records = page.getRecords().stream()
                .map(p -> toPetVO(p, categoryNameMap))
                .toList();
        return PageResult.of(page, records);
    }

    @Override
    public PetDetailVO getDetail(Long id) {
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            throw new BusinessException("宠物信息不存在");
        }

        // 使用原子更新避免并发问题
        petMapper.update(null,
                new LambdaUpdateWrapper<Pet>()
                        .eq(Pet::getId, id)
                        .setSql("view_count = view_count + 1"));

        PetDetailVO vo = new PetDetailVO();
        copyFromPet(vo, pet, null);

        List<PetImage> images = petImageMapper.selectList(
                new LambdaQueryWrapper<PetImage>()
                        .eq(PetImage::getPetId, id)
                        .orderByAsc(PetImage::getSortOrder)
        );
        vo.setImages(images.stream().map(PetImage::getUrl).toList());

        return vo;
    }

    @Override
    public Pet getById(Long id) {
        return petMapper.selectById(id);
    }

    @Override
    public PetVO create(Pet pet) {
        pet.setViewCount(0);
        if (!StringUtils.hasText(pet.getStatus())) {
            pet.setStatus("AVAILABLE");
        }
        petMapper.insert(pet);
        Map<Long, String> catMap = buildCategoryNameMap(List.of(pet));
        return toPetVO(pet, catMap);
    }

    @Override
    public PetVO update(Pet pet, Long userId, boolean isAdmin) {
        Pet existing = petMapper.selectById(pet.getId());
        if (existing == null) {
            throw new BusinessException("宠物信息不存在");
        }
        // 管理员可编辑任何宠物，普通用户仅可编辑自己发布的
        if (!isAdmin && existing.getCreatedBy() != null && !existing.getCreatedBy().equals(userId)) {
            throw new BusinessException("无权编辑此宠物信息");
        }

        // 只允许更新指定字段，防止覆盖敏感字段
        if (StringUtils.hasText(pet.getName())) existing.setName(pet.getName());
        if (pet.getCategoryId() != null) existing.setCategoryId(pet.getCategoryId());
        if (pet.getBreed() != null) existing.setBreed(pet.getBreed());
        if (pet.getAge() != null) existing.setAge(pet.getAge());
        if (pet.getGender() != null) existing.setGender(pet.getGender());
        if (pet.getSize() != null) existing.setSize(pet.getSize());
        if (pet.getColor() != null) existing.setColor(pet.getColor());
        if (pet.getHealthStatus() != null) existing.setHealthStatus(pet.getHealthStatus());
        if (pet.getVaccination() != null) existing.setVaccination(pet.getVaccination());
        if (pet.getSterilization() != null) existing.setSterilization(pet.getSterilization());
        if (pet.getDescription() != null) existing.setDescription(pet.getDescription());
        if (pet.getCoverImage() != null) existing.setCoverImage(pet.getCoverImage());

        petMapper.updateById(existing);
        Map<Long, String> catMap = buildCategoryNameMap(List.of(existing));
        return toPetVO(existing, catMap);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId, boolean isAdmin) {
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            throw new BusinessException("宠物信息不存在");
        }
        // 管理员可删除任何宠物，普通用户仅可删除自己发布的
        if (!isAdmin && pet.getCreatedBy() != null && !pet.getCreatedBy().equals(userId)) {
            throw new BusinessException("无权删除此宠物信息");
        }
        // 先删图片记录，再删宠物
        petImageMapper.delete(new LambdaQueryWrapper<PetImage>().eq(PetImage::getPetId, id));
        petMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, String status) {
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            throw new BusinessException("宠物信息不存在");
        }
        pet.setStatus(status);
        petMapper.updateById(pet);
    }

    @Override
    public PageResult<PetVO> getUserPets(Long userId, int page, int size) {
        Page<Pet> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<Pet>()
                .eq(Pet::getCreatedBy, userId)
                .orderByDesc(Pet::getCreatedAt);
        petMapper.selectPage(pageObj, wrapper);

        Map<Long, String> categoryNameMap = buildCategoryNameMap(pageObj.getRecords());
        List<PetVO> records = pageObj.getRecords().stream()
                .map(p -> toPetVO(p, categoryNameMap))
                .toList();
        return new PageResult<>(records, pageObj.getTotal(), pageObj.getCurrent(), pageObj.getSize(), pageObj.getPages());
    }

    /** 批量构建分类名称映射，避免 N+1 */
    private Map<Long, String> buildCategoryNameMap(List<Pet> pets) {
        List<Long> categoryIds = pets.stream()
                .map(Pet::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (categoryIds.isEmpty()) return Map.of();
        return categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
    }

    private PetVO toPetVO(Pet pet, Map<Long, String> catNameMap) {
        PetVO vo = new PetVO();
        String catName = catNameMap != null ? catNameMap.get(pet.getCategoryId()) : null;
        copyFromPet(vo, pet, catName);
        return vo;
    }

    private void copyFromPet(PetVO vo, Pet pet, String categoryName) {
        vo.setId(pet.getId());
        vo.setName(pet.getName());
        vo.setCategoryId(pet.getCategoryId());
        vo.setCategoryName(categoryName);
        vo.setBreed(pet.getBreed());
        vo.setAge(pet.getAge());
        vo.setGender(pet.getGender());
        vo.setSize(pet.getSize());
        vo.setColor(pet.getColor());
        vo.setHealthStatus(pet.getHealthStatus());
        vo.setVaccination(pet.getVaccination());
        vo.setSterilization(pet.getSterilization());
        vo.setDescription(pet.getDescription());
        vo.setCoverImage(pet.getCoverImage());
        vo.setStatus(pet.getStatus());
        vo.setViewCount(pet.getViewCount());
        vo.setCreatedAt(pet.getCreatedAt());
        vo.setCreatedBy(pet.getCreatedBy());
    }
}
