package com.petadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.entity.User;
import com.petadoption.exception.BusinessException;
import com.petadoption.mapper.UserMapper;
import com.petadoption.service.CertService;
import com.petadoption.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CertServiceImpl implements CertService {

    private final UserMapper userMapper;

    @Override
    @Transactional
    public void applyCert(Long userId, String shelterName, String shelterAddress, String certData) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (!"SHELTER".equals(user.getRole())) throw new BusinessException("仅机构用户可以申请认证");
        if ("PENDING".equals(user.getCertStatus())) throw new BusinessException("已有认证申请正在审核中");
        user.setShelterName(shelterName);
        user.setShelterAddress(shelterAddress);
        user.setCertData(certData);
        user.setCertStatus("PENDING");
        userMapper.updateById(user);
    }

    @Override
    public Page<UserVO> getCertList(int page, int size, String status) {
        Page<User> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getRole, "SHELTER");
        if (status != null && !status.isEmpty()) {
            wrapper.eq(User::getCertStatus, status);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        userMapper.selectPage(pageObj, wrapper);
        Page<UserVO> result = new Page<>(page, size, pageObj.getTotal());
        result.setRecords(pageObj.getRecords().stream().map(this::toUserVO).toList());
        return result;
    }

    @Override
    @Transactional
    public void reviewCert(Long userId, String status, String comment) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setCertStatus(status);
        // 保存审核备注到 certData（追加方式）
        if (comment != null && !comment.isEmpty()) {
            String existing = user.getCertData() != null ? user.getCertData() : "";
            user.setCertData(existing + "\n[审核备注] " + comment);
        }
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
