package com.petadoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadoption.entity.Review;
import com.petadoption.entity.Blacklist;
import com.petadoption.entity.Adoption;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.Result;
import com.petadoption.mapper.ReviewMapper;
import com.petadoption.mapper.BlacklistMapper;
import com.petadoption.mapper.AdoptionMapper;
import com.petadoption.mapper.UserMapper;
import com.petadoption.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trust")
@RequiredArgsConstructor
public class TrustController {

    private final ReviewMapper reviewMapper;
    private final BlacklistMapper blacklistMapper;
    private final AdoptionMapper adoptionMapper;
    private final UserMapper userMapper;

    @PostMapping("/reviews")
    public Result<String> createReview(Authentication auth, @RequestBody Map<String, Object> body) {
        Long userId = (Long) auth.getPrincipal();
        Long adoptionId = Long.valueOf(body.get("adoptionId").toString());
        Adoption adoption = adoptionMapper.selectById(adoptionId);
        if (adoption == null) throw new BusinessException("领养记录不存在");
        if (!"COMPLETED".equals(adoption.getStatus())) throw new BusinessException("领养未完成，无法评价");

        // 判断评价方向
        boolean isAdopter = adoption.getUserId().equals(userId);
        boolean isOwner = false;
        // 如果是送养方（通过pet找到owner）
        // 简化：检查已有评价
        long count = reviewMapper.selectCount(
            new LambdaQueryWrapper<Review>()
                .eq(Review::getAdoptionId, adoptionId)
                .eq(Review::getReviewerId, userId));
        if (count > 0) throw new BusinessException("您已评价过");

        Review review = new Review();
        review.setReviewerId(userId);
        review.setTargetId(isAdopter ? adoption.getUserId() : userId); // simplified
        review.setAdoptionId(adoptionId);
        review.setRoleType(isAdopter ? "ADOPTER" : "SHELTER");
        review.setRating(Integer.parseInt(body.getOrDefault("rating", "5").toString()));
        review.setContent((String) body.get("content"));
        reviewMapper.insert(review);
        return Result.success("评价成功");
    }

    @GetMapping("/reviews/user/{userId}")
    public Result<List<Map<String, Object>>> getUserReviews(@PathVariable Long userId) {
        List<Review> reviews = reviewMapper.selectList(
            new LambdaQueryWrapper<Review>().eq(Review::getTargetId, userId)
                .orderByDesc(Review::getCreatedAt));
        return Result.success(reviews.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("reviewerId", r.getReviewerId());
            m.put("rating", r.getRating());
            m.put("content", r.getContent());
            m.put("roleType", r.getRoleType());
            m.put("createdAt", r.getCreatedAt());
            User reviewer = userMapper.selectById(r.getReviewerId());
            m.put("reviewerName", reviewer != null ? (reviewer.getNickname() != null ? reviewer.getNickname() : reviewer.getUsername()) : "");
            return m;
        }).collect(Collectors.toList()));
    }

    @GetMapping("/reviews/my")
    public Result<List<Map<String, Object>>> getMyReviews(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return getUserReviews(userId);
    }

    @PostMapping("/blacklist")
    @PreAuthorize("hasAnyRole('SHELTER', 'ADMIN')")
    public Result<Void> addBlacklist(Authentication auth, @RequestBody Map<String, Object> body) {
        Long userId = (Long) auth.getPrincipal();
        Long targetId = Long.valueOf(body.get("targetId").toString());
        String reason = (String) body.get("reason");

        long count = blacklistMapper.selectCount(
            new LambdaQueryWrapper<Blacklist>()
                .eq(Blacklist::getReporterId, userId)
                .eq(Blacklist::getTargetId, targetId));
        if (count > 0) throw new BusinessException("该用户已在您的黑名单中");

        Blacklist bl = new Blacklist();
        bl.setReporterId(userId);
        bl.setTargetId(targetId);
        bl.setReason(reason);
        blacklistMapper.insert(bl);
        return Result.success();
    }

    @GetMapping("/blacklist")
    @PreAuthorize("hasAnyRole('SHELTER', 'ADMIN')")
    public Result<List<Map<String, Object>>> getBlacklist(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        List<Blacklist> list = blacklistMapper.selectList(
            new LambdaQueryWrapper<Blacklist>().eq(Blacklist::getReporterId, userId));
        return Result.success(list.stream().map(bl -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", bl.getId());
            m.put("targetId", bl.getTargetId());
            m.put("reason", bl.getReason());
            m.put("createdAt", bl.getCreatedAt());
            User target = userMapper.selectById(bl.getTargetId());
            m.put("targetName", target != null ? (target.getNickname() != null ? target.getNickname() : target.getUsername()) : "");
            return m;
        }).collect(Collectors.toList()));
    }

    @DeleteMapping("/blacklist/{id}")
    @PreAuthorize("hasAnyRole('SHELTER', 'ADMIN')")
    public Result<Void> removeBlacklist(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Blacklist bl = blacklistMapper.selectById(id);
        if (bl == null || !bl.getReporterId().equals(userId))
            throw new BusinessException("无权操作");
        blacklistMapper.deleteById(id);
        return Result.success();
    }
}
