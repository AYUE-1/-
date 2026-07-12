package com.petadoption.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.exception.Result;
import com.petadoption.service.UserService;
import com.petadoption.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public Result<Map<String, Object>> userList(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        Page<UserVO> result = userService.getUserList(page, size);
        return Result.success(Map.of(
                "records", result.getRecords(),
                "total", result.getTotal(),
                "page", result.getCurrent(),
                "size", result.getSize(),
                "pages", result.getPages()
        ));
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam String status) {
        userService.updateUserStatus(id, status);
        return Result.success();
    }
}
