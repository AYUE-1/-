package com.petadoption.controller;

import com.petadoption.dto.LoginDTO;
import com.petadoption.dto.RegisterDTO;
import com.petadoption.exception.Result;
import com.petadoption.service.AuthService;
import com.petadoption.vo.LoginResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResultVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @PostMapping("/register")
    public Result<LoginResultVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.success(authService.register(dto));
    }
}
