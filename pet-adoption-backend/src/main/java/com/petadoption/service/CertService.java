package com.petadoption.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.entity.User;
import com.petadoption.vo.UserVO;

public interface CertService {
    void applyCert(Long userId, String shelterName, String shelterAddress, String certData);
    Page<UserVO> getCertList(int page, int size, String status);
    void reviewCert(Long userId, String status, String comment);
}
