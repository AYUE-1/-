package com.petadoption.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadoption.entity.Notification;
import com.petadoption.exception.Result;
import com.petadoption.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationMapper notificationMapper;

    @GetMapping
    public Result<List<Notification>> list(Authentication auth,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(notificationMapper.selectList(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt)
                .last("LIMIT " + size + " OFFSET " + ((page - 1) * size))
        ));
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Object>> unreadCount(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Long count = notificationMapper.selectCount(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
        return Result.success(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Notification notif = notificationMapper.selectById(id);
        if (notif != null && notif.getUserId().equals(userId)) {
            notif.setIsRead(1);
            notificationMapper.updateById(notif);
        }
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllRead(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        List<Notification> list = notificationMapper.selectList(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
        for (Notification n : list) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
        return Result.success();
    }
}
