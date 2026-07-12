package com.petadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petadoption.dto.LoginDTO;
import com.petadoption.dto.RegisterDTO;
import com.petadoption.entity.User;
import com.petadoption.exception.BusinessException;
import com.petadoption.exception.ResultCode;
import com.petadoption.mapper.UserMapper;
import com.petadoption.service.AuthService;
import com.petadoption.utils.JwtUtils;
import com.petadoption.utils.PasswordUtils;
import com.petadoption.vo.LoginResultVO;
import com.petadoption.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;

    @Override
    public LoginResultVO login(LoginDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())
        );
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "账号已被禁用");
        }
        if (!PasswordUtils.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        UserVO userVO = toUserVO(user);
        return new LoginResultVO(token, userVO);
    }

    @Override
    public LoginResultVO register(RegisterDTO dto) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(PasswordUtils.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        // 允许注册时选择角色，默认USER
        String role = dto.getRole();
        if (role == null || (!role.equals("USER") && !role.equals("SHELTER"))) {
            role = "USER";
        }
        user.setRole(role);
        user.setStatus(1);
        userMapper.insert(user);

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        UserVO userVO = toUserVO(user);
        return new LoginResultVO(token, userVO);
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus() != null && user.getStatus() == 1 ? "ACTIVE" : "DISABLED");
        vo.setBio(user.getBio());
        vo.setCertStatus(user.getCertStatus());
        vo.setCertData(user.getCertData());
        vo.setShelterName(user.getShelterName());
        vo.setShelterAddress(user.getShelterAddress());
        return vo;
    }
}
