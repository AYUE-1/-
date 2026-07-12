package com.petadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.entity.User;
import com.petadoption.exception.BusinessException;
import com.petadoption.mapper.UserMapper;
import com.petadoption.service.UserService;
import com.petadoption.utils.PasswordUtils;
import com.petadoption.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public UserVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toUserVO(user);
    }

    @Override
    public UserVO updateProfile(Long userId, User updated) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setNickname(updated.getNickname());
        user.setEmail(updated.getEmail());
        user.setPhone(updated.getPhone());
        if (updated.getBio() != null) user.setBio(updated.getBio());
        if (updated.getShelterName() != null) user.setShelterName(updated.getShelterName());
        if (updated.getShelterAddress() != null) user.setShelterAddress(updated.getShelterAddress());
        userMapper.updateById(user);
        return toUserVO(user);
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!PasswordUtils.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(PasswordUtils.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
    }

    @Override
    public Page<UserVO> getUserList(int page, int size) {
        Page<User> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreatedAt);
        userMapper.selectPage(pageObj, wrapper);

        Page<UserVO> resultPage = new Page<>(page, size, pageObj.getTotal());
        resultPage.setRecords(pageObj.getRecords().stream().map(this::toUserVO).toList());
        return resultPage;
    }

    @Override
    public void updateUserStatus(Long id, String status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus("ACTIVE".equals(status) ? 1 : 0);
        userMapper.updateById(user);
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
