package com.petadoption.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.dto.AdoptionApplyDTO;
import com.petadoption.dto.AdoptionReviewDTO;
import com.petadoption.exception.Result;
import com.petadoption.service.AdoptionService;
import com.petadoption.vo.AdoptionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/adoptions")
@RequiredArgsConstructor
public class AdoptionController {

    private final AdoptionService adoptionService;

    /** 用户提交领养申请 */
    @PostMapping
    public Result<AdoptionVO> apply(Authentication auth, @Valid @RequestBody AdoptionApplyDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(adoptionService.apply(userId, dto));
    }

    /** 用户查看自己的申请列表 */
    @GetMapping("/my")
    public Result<Map<String, Object>> myApplications(Authentication auth,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) auth.getPrincipal();
        Page<AdoptionVO> result = adoptionService.getMyApplications(userId, page, size);
        return Result.success(Map.of(
                "records", result.getRecords(),
                "total", result.getTotal(),
                "page", result.getCurrent(),
                "size", result.getSize(),
                "pages", result.getPages()
        ));
    }

    /** 用户查看自己发布的宠物的领养申请（发布者视角） */
    @GetMapping("/owner")
    public Result<Map<String, Object>> ownerApplications(Authentication auth,
                                                          @RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(required = false) String status) {
        Long ownerId = (Long) auth.getPrincipal();
        Page<AdoptionVO> result = adoptionService.getOwnerApplications(ownerId, page, size, status);
        return Result.success(Map.of(
                "records", result.getRecords(),
                "total", result.getTotal(),
                "page", result.getCurrent(),
                "size", result.getSize(),
                "pages", result.getPages()
        ));
    }

    /** 查看申请详情 */
    @GetMapping("/{id}")
    public Result<AdoptionVO> detail(@PathVariable Long id) {
        return Result.success(adoptionService.getDetail(id));
    }

    /** 申请人取消申请 */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        adoptionService.cancel(id, userId);
        return Result.success();
    }

    /** 管理员查看所有申请 */
    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> adminList(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) String status) {
        Page<AdoptionVO> result = adoptionService.getAdminList(page, size, status);
        return Result.success(Map.of(
                "records", result.getRecords(),
                "total", result.getTotal(),
                "page", result.getCurrent(),
                "size", result.getSize(),
                "pages", result.getPages()
        ));
    }

    /** 管理员初审：通过→待发布者确认，拒绝→结束 */
    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> review(@PathVariable Long id,
                                @Valid @RequestBody AdoptionReviewDTO dto,
                                Authentication auth) {
        Long reviewerId = (Long) auth.getPrincipal();
        adoptionService.review(id, dto, reviewerId);
        return Result.success();
    }

    /** 发布者终审：同意→领养达成，拒绝→退回 */
    @PutMapping("/{id}/owner-review")
    public Result<Void> ownerReview(@PathVariable Long id,
                                     @Valid @RequestBody AdoptionReviewDTO dto,
                                     Authentication auth) {
        Long ownerId = (Long) auth.getPrincipal();
        adoptionService.ownerReview(id, dto, ownerId);
        return Result.success();
    }
}
