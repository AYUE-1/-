package com.petadoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadoption.entity.Adoption;
import com.petadoption.entity.Pet;
import com.petadoption.entity.User;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.Result;
import com.petadoption.mapper.AdoptionMapper;
import com.petadoption.mapper.PetMapper;
import com.petadoption.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shelter")
@RequiredArgsConstructor
public class ShelterController {

    private final PetMapper petMapper;
    private final AdoptionMapper adoptionMapper;
    private final UserMapper userMapper;

    /** 机构工作台数据概览 */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userMapper.selectById(userId);
        if (user == null || !"SHELTER".equals(user.getRole()))
            throw new BusinessException("仅机构用户可访问");

        // 我发布的宠物总数
        long petTotal = petMapper.selectCount(
            new LambdaQueryWrapper<Pet>().eq(Pet::getCreatedBy, userId));

        // 在架宠物数
        long availableCount = petMapper.selectCount(
            new LambdaQueryWrapper<Pet>()
                .eq(Pet::getCreatedBy, userId)
                .eq(Pet::getStatus, "AVAILABLE"));

        // 已领养数
        long adoptedCount = petMapper.selectCount(
            new LambdaQueryWrapper<Pet>()
                .eq(Pet::getCreatedBy, userId)
                .eq(Pet::getStatus, "ADOPTED"));

        // 待我处理的领养申请数（状态为 ADMIN_APPROVED，等待 owner review）
        // 先找出我发布的宠物ID
        List<Long> myPetIds = petMapper.selectList(
            new LambdaQueryWrapper<Pet>().eq(Pet::getCreatedBy, userId))
            .stream().map(Pet::getId).collect(Collectors.toList());

        long pendingReview = 0;
        if (!myPetIds.isEmpty()) {
            pendingReview = adoptionMapper.selectCount(
                new LambdaQueryWrapper<Adoption>()
                    .in(Adoption::getPetId, myPetIds)
                    .eq(Adoption::getStatus, "ADMIN_APPROVED"));
        }

        // 总申请数
        long totalApplications = 0;
        if (!myPetIds.isEmpty()) {
            totalApplications = adoptionMapper.selectCount(
                new LambdaQueryWrapper<Adoption>()
                    .in(Adoption::getPetId, myPetIds));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("petTotal", petTotal);
        result.put("availableCount", availableCount);
        result.put("adoptedCount", adoptedCount);
        result.put("pendingReview", pendingReview);
        result.put("totalApplications", totalApplications);
        result.put("certStatus", user.getCertStatus());
        result.put("shelterName", user.getShelterName());
        result.put("nickname", user.getNickname());
        return Result.success(result);
    }

    /** 公开的机构主页 */
    @GetMapping("/profile/{userId}")
    public Result<Map<String, Object>> profile(@PathVariable Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !"SHELTER".equals(user.getRole()))
            throw new BusinessException("该机构不存在");

        // 在架宠物
        List<Pet> availablePets = petMapper.selectList(
            new LambdaQueryWrapper<Pet>()
                .eq(Pet::getCreatedBy, userId)
                .eq(Pet::getStatus, "AVAILABLE")
                .orderByDesc(Pet::getCreatedAt));

        List<Map<String, Object>> petList = availablePets.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("breed", p.getBreed());
            m.put("age", p.getAge());
            m.put("gender", p.getGender());
            m.put("petSize", p.getSize());
            m.put("coverImage", p.getCoverImage());
            m.put("status", p.getStatus());
            m.put("viewCount", p.getViewCount());
            return m;
        }).collect(Collectors.toList());

        // 已领养数
        long adoptedCount = petMapper.selectCount(
            new LambdaQueryWrapper<Pet>()
                .eq(Pet::getCreatedBy, userId)
                .eq(Pet::getStatus, "ADOPTED"));

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("nickname", user.getNickname());
        result.put("shelterName", user.getShelterName());
        result.put("shelterAddress", user.getShelterAddress());
        result.put("certStatus", user.getCertStatus());
        result.put("avatar", user.getAvatar());
        result.put("bio", user.getBio());
        result.put("adoptedCount", adoptedCount);
        result.put("petCount", availablePets.size());
        result.put("pets", petList);
        return Result.success(result);
    }
}
