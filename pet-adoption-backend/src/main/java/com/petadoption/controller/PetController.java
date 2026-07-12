package com.petadoption.controller;

import com.petadoption.dto.PageResult;
import com.petadoption.dto.PetQueryDTO;
import com.petadoption.entity.Pet;
import com.petadoption.exception.Result;
import com.petadoption.service.PetService;
import com.petadoption.vo.PetDetailVO;
import com.petadoption.vo.PetVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @GetMapping
    public Result<PageResult<PetVO>> list(PetQueryDTO dto) {
        return Result.success(petService.queryPets(dto));
    }

    @GetMapping("/{id}")
    public Result<PetDetailVO> detail(@PathVariable Long id) {
        return Result.success(petService.getDetail(id));
    }

    /** 普通用户查看自己发布的宠物 */
    @GetMapping("/my")
    public Result<PageResult<PetVO>> myPets(Authentication auth,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(petService.getUserPets(userId, page, size));
    }

    /** 普通用户发布领养宠物 */
    @PostMapping
    public Result<PetVO> create(Authentication auth, @RequestBody Pet pet) {
        Long userId = (Long) auth.getPrincipal();
        pet.setCreatedBy(userId);
        return Result.success(petService.create(pet));
    }

    /** 更新宠物信息（发布者本人或管理员可操作） */
    @PutMapping("/{id}")
    public Result<PetVO> update(Authentication auth, @PathVariable Long id, @RequestBody Pet pet) {
        Long userId = (Long) auth.getPrincipal();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        pet.setId(id);
        return Result.success(petService.update(pet, userId, isAdmin));
    }

    /** 删除宠物（发布者本人或管理员可操作） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(Authentication auth, @PathVariable Long id) {
        Long userId = (Long) auth.getPrincipal();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        petService.delete(id, userId, isAdmin);
        return Result.success();
    }

    /** 管理员修改宠物状态 */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        petService.updateStatus(id, status);
        return Result.success();
    }
}
