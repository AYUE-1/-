package com.petadoption.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.dto.AdoptionApplyDTO;
import com.petadoption.dto.AdoptionReviewDTO;
import com.petadoption.entity.Adoption;
import com.petadoption.vo.AdoptionVO;

public interface AdoptionService {
    AdoptionVO apply(Long userId, AdoptionApplyDTO dto);
    Page<AdoptionVO> getMyApplications(Long userId, int page, int size);
    AdoptionVO getDetail(Long id);
    void cancel(Long id, Long userId);
    Page<AdoptionVO> getAdminList(int page, int size, String status);
    /** 管理员初审：PENDING → ADMIN_APPROVED 或 REJECTED */
    void review(Long id, AdoptionReviewDTO dto, Long reviewerId);
    /** 发布者终审：ADMIN_APPROVED → APPROVED 或 OWNER_REJECTED */
    void ownerReview(Long id, AdoptionReviewDTO dto, Long ownerId);
    /** 发布者查看自己宠物的领养申请 */
    Page<AdoptionVO> getOwnerApplications(Long ownerId, int page, int size, String status);
}
