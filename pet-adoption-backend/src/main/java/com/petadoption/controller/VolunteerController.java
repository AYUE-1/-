package com.petadoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadoption.entity.Volunteer;
import com.petadoption.entity.User;
import com.petadoption.entity.RescuePost;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.Result;
import com.petadoption.mapper.VolunteerMapper;
import com.petadoption.mapper.UserMapper;
import com.petadoption.mapper.RescuePostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerMapper volunteerMapper;
    private final UserMapper userMapper;
    private final RescuePostMapper rescuePostMapper;

    /** 用户申请志愿者——自动通过，无需管理员审核 */
    @PostMapping("/api/welfare/volunteer/apply")
    public Result<String> apply(Authentication auth, @RequestBody Map<String, String> body) {
        Long userId = (Long) auth.getPrincipal();
        Volunteer existing = volunteerMapper.selectOne(
            new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getUserId, userId));
        if (existing != null) {
            if ("ACTIVE".equals(existing.getStatus())) throw new BusinessException("您已是认证志愿者");
            // PENDING (旧数据) 或 REJECTED 都直接改为 ACTIVE
            existing.setStatus("ACTIVE");
            existing.setApprovedAt(LocalDateTime.now());
            existing.setSkills(body.get("skills"));
            existing.setAvailableTime(body.get("availableTime"));
            existing.setRegion(body.get("region"));
            volunteerMapper.updateById(existing);
            return Result.success("恭喜，您已成为认证志愿者！");
        }

        Volunteer v = new Volunteer();
        v.setUserId(userId);
        v.setSkills(body.get("skills"));
        v.setAvailableTime(body.get("availableTime"));
        v.setRegion(body.get("region"));
        v.setStatus("ACTIVE");
        v.setApprovedAt(LocalDateTime.now());
        volunteerMapper.insert(v);
        return Result.success("恭喜，您已成为认证志愿者！");
    }

    /** 用户查看自己的志愿者状态（含救助数量） */
    @GetMapping("/api/welfare/volunteer/my")
    public Result<Map<String, Object>> myInfo(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Volunteer v = volunteerMapper.selectOne(
            new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getUserId, userId));
        if (v == null) return Result.success(null);

        long rescueCount = rescuePostMapper.selectCount(
            new LambdaQueryWrapper<RescuePost>()
                .eq(RescuePost::getClaimedBy, userId)
                .eq(RescuePost::getStatus, "RESCUED"));

        Map<String, Object> m = new HashMap<>();
        m.put("id", v.getId());
        m.put("userId", v.getUserId());
        m.put("skills", v.getSkills());
        m.put("availableTime", v.getAvailableTime());
        m.put("region", v.getRegion());
        m.put("status", v.getStatus());
        m.put("welfareActivities", v.getWelfareActivities());
        m.put("approvedAt", v.getApprovedAt());
        m.put("createdAt", v.getCreatedAt());
        m.put("rescueCount", rescueCount);
        return Result.success(m);
    }

    /** 公开的已通过志愿者列表（含救助数量，按救助数降序） */
    @GetMapping("/api/welfare/volunteers")
    public Result<List<Map<String, Object>>> list() {
        List<Volunteer> volunteers = volunteerMapper.selectList(
            new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getStatus, "ACTIVE"));
        return Result.success(volunteers.stream().map(v -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", v.getId());
            m.put("userId", v.getUserId());
            m.put("skills", v.getSkills());
            m.put("availableTime", v.getAvailableTime());
            m.put("region", v.getRegion());
            m.put("status", v.getStatus());
            m.put("welfareActivities", v.getWelfareActivities());
            m.put("approvedAt", v.getApprovedAt());
            User u = userMapper.selectById(v.getUserId());
            m.put("userName", u != null ? (u.getNickname() != null ? u.getNickname() : u.getUsername()) : "");
            m.put("avatar", u != null ? u.getAvatar() : "");

            long rescueCount = rescuePostMapper.selectCount(
                new LambdaQueryWrapper<RescuePost>()
                    .eq(RescuePost::getClaimedBy, v.getUserId())
                    .eq(RescuePost::getStatus, "RESCUED"));
            m.put("rescueCount", rescueCount);
            return m;
        }).sorted((a, b) -> Long.compare((long) b.get("rescueCount"), (long) a.get("rescueCount")))
          .collect(Collectors.toList()));
    }

    /** 公开的志愿者详情页（含救助动物列表） */
    @GetMapping("/api/welfare/volunteer/{id}")
    public Result<Map<String, Object>> volunteerDetail(@PathVariable Long id) {
        Volunteer v = volunteerMapper.selectById(id);
        if (v == null || !"ACTIVE".equals(v.getStatus()))
            throw new BusinessException("该志愿者不存在");

        User u = userMapper.selectById(v.getUserId());

        long rescueCount = rescuePostMapper.selectCount(
            new LambdaQueryWrapper<RescuePost>()
                .eq(RescuePost::getClaimedBy, v.getUserId())
                .eq(RescuePost::getStatus, "RESCUED"));

        List<RescuePost> rescuedAnimals = rescuePostMapper.selectList(
            new LambdaQueryWrapper<RescuePost>()
                .eq(RescuePost::getClaimedBy, v.getUserId())
                .eq(RescuePost::getStatus, "RESCUED")
                .orderByDesc(RescuePost::getRescuedAt));

        List<Map<String, Object>> animalList = rescuedAnimals.stream().map(r -> {
            Map<String, Object> am = new HashMap<>();
            am.put("id", r.getId());
            am.put("title", r.getTitle());
            am.put("animalType", r.getAnimalType());
            am.put("animalDesc", r.getAnimalDesc());
            am.put("rescuedAt", r.getRescuedAt());
            am.put("followUpText", r.getFollowUpText());
            return am;
        }).collect(Collectors.toList());

        Map<String, Object> m = new HashMap<>();
        m.put("id", v.getId());
        m.put("userId", v.getUserId());
        m.put("skills", v.getSkills());
        m.put("availableTime", v.getAvailableTime());
        m.put("region", v.getRegion());
        m.put("status", v.getStatus());
        m.put("welfareActivities", v.getWelfareActivities());
        m.put("approvedAt", v.getApprovedAt());
        m.put("createdAt", v.getCreatedAt());
        m.put("userName", u != null ? (u.getNickname() != null ? u.getNickname() : u.getUsername()) : "");
        m.put("avatar", u != null ? u.getAvatar() : "");
        m.put("rescueCount", rescueCount);
        m.put("rescuedAnimals", animalList);
        return Result.success(m);
    }

    /** 志愿者编辑自己的信息 */
    @PutMapping("/api/welfare/volunteer/my")
    public Result<String> updateMyVolunteer(Authentication auth, @RequestBody Map<String, String> body) {
        Long userId = (Long) auth.getPrincipal();
        Volunteer v = volunteerMapper.selectOne(
            new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getUserId, userId));
        if (v == null || !"ACTIVE".equals(v.getStatus()))
            throw new BusinessException("您不是认证志愿者，无法编辑");

        if (body.containsKey("skills")) v.setSkills(body.get("skills"));
        if (body.containsKey("availableTime")) v.setAvailableTime(body.get("availableTime"));
        if (body.containsKey("region")) v.setRegion(body.get("region"));
        if (body.containsKey("welfareActivities")) v.setWelfareActivities(body.get("welfareActivities"));
        volunteerMapper.updateById(v);
        return Result.success("志愿者信息已更新");
    }

    /** 公益板块统计数据 */
    @GetMapping("/api/welfare/stats")
    public Result<Map<String, Object>> stats() {
        long total = volunteerMapper.selectCount(null);
        long active = volunteerMapper.selectCount(
            new LambdaQueryWrapper<Volunteer>().eq(Volunteer::getStatus, "ACTIVE"));
        long rescuedCount = rescuePostMapper.selectCount(
            new LambdaQueryWrapper<RescuePost>().eq(RescuePost::getStatus, "RESCUED"));
        return Result.success(Map.of(
            "volunteerCount", active,
            "totalApplications", total,
            "rescuedCount", rescuedCount,
            "message", "每一份爱心都值得被看见"
        ));
    }
}
