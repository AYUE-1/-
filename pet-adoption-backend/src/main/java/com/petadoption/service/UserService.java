package com.petadoption.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.entity.User;
import com.petadoption.vo.UserVO;

public interface UserService {
    UserVO getProfile(Long userId);
    UserVO updateProfile(Long userId, User user);
    void updatePassword(Long userId, String oldPassword, String newPassword);
    void updateAvatar(Long userId, String avatarUrl);
    Page<UserVO> getUserList(int page, int size);
    void updateUserStatus(Long id, String status);
}
