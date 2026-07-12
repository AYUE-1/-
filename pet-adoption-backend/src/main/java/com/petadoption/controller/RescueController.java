package com.petadoption.controller;

import com.petadoption.dto.RescuePostDTO;
import com.petadoption.dto.RescueQueryDTO;
import com.petadoption.exception.Result;
import com.petadoption.service.RescueService;
import com.petadoption.vo.RescueDetailVO;
import com.petadoption.vo.RescuePostVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rescue")
@RequiredArgsConstructor
public class RescueController {

    private final RescueService rescueService;

    @PostMapping
    public Result<RescuePostVO> create(Authentication auth, @Valid @RequestBody RescuePostDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(rescueService.create(userId, dto));
    }

    @GetMapping
    public Result<List<RescuePostVO>> list(RescueQueryDTO dto) {
        return Result.success(rescueService.queryList(dto));
    }

    @GetMapping("/{id}")
    public Result<RescueDetailVO> detail(@PathVariable Long id) {
        return Result.success(rescueService.getDetail(id));
    }

    @PutMapping("/{id}")
    public Result<RescuePostVO> update(@PathVariable Long id, Authentication auth,
                                        @Valid @RequestBody RescuePostDTO dto) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(rescueService.update(id, userId, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        rescueService.delete(id, userId);
        return Result.success();
    }

    @PostMapping("/{id}/claim")
    public Result<String> claim(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        rescueService.claim(id, userId);
        return Result.success("认领成功");
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, Authentication auth,
                                      @RequestBody Map<String, String> body) {
        Long userId = (Long) auth.getPrincipal();
        rescueService.updateStatus(id, userId, body.get("status"));
        return Result.success();
    }

    @PostMapping("/{id}/follow-up")
    public Result<Void> followUp(@PathVariable Long id, Authentication auth,
                                  @RequestBody Map<String, String> body) {
        Long userId = (Long) auth.getPrincipal();
        rescueService.addFollowUp(id, userId, body.get("content"));
        return Result.success();
    }

    @GetMapping("/my")
    public Result<List<RescuePostVO>> myRescues(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(rescueService.getMyRescues(userId));
    }

    @GetMapping("/my-claims")
    public Result<List<RescuePostVO>> myClaims(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(rescueService.getMyClaims(userId));
    }
}
