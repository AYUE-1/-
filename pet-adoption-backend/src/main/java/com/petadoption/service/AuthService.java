package com.petadoption.service;

import com.petadoption.dto.LoginDTO;
import com.petadoption.dto.RegisterDTO;
import com.petadoption.vo.LoginResultVO;

public interface AuthService {
    LoginResultVO login(LoginDTO dto);
    LoginResultVO register(RegisterDTO dto);
}
