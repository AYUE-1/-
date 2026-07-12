package com.petadoption.controller;

import com.petadoption.entity.User;
import com.petadoption.exception.Result;
import com.petadoption.service.UserService;
import com.petadoption.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public Result<UserVO> profile(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(Authentication auth, @RequestBody User user) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(userService.updateProfile(userId, user));
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(Authentication auth, @RequestBody Map<String, String> body) {
        Long userId = (Long) auth.getPrincipal();
        userService.updatePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return Result.success();
    }

    @PutMapping("/avatar")
    public Result<Void> updateAvatar(Authentication auth, @RequestBody Map<String, String> body) {
        Long userId = (Long) auth.getPrincipal();
        userService.updateAvatar(userId, body.get("avatar"));
        return Result.success();
    }
}
