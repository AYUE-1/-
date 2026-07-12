package com.petadoption.service;

import com.petadoption.dto.RescuePostDTO;
import com.petadoption.dto.RescueQueryDTO;
import com.petadoption.vo.RescueDetailVO;
import com.petadoption.vo.RescuePostVO;

import java.util.List;

public interface RescueService {
    RescuePostVO create(Long userId, RescuePostDTO dto);
    RescueDetailVO getDetail(Long id);
    List<RescuePostVO> queryList(RescueQueryDTO dto);
    RescuePostVO update(Long id, Long userId, RescuePostDTO dto);
    void delete(Long id, Long userId);
    void claim(Long id, Long userId);
    void updateStatus(Long id, Long userId, String status);
    void addFollowUp(Long id, Long userId, String followUpText);
    List<RescuePostVO> getMyRescues(Long userId);
    List<RescuePostVO> getMyClaims(Long userId);
}
