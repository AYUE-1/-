package com.petadoption.controller;

import com.petadoption.entity.CommunityPost;
import com.petadoption.exception.Result;
import com.petadoption.service.CommunityService;
import com.petadoption.vo.CommunityPostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping("/posts")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sortBy) {
        return Result.success(communityService.queryPosts(page, size, type, sortBy));
    }

    @GetMapping("/posts/{id}")
    public Result<CommunityPostVO> detail(@PathVariable Long id, Authentication auth) {
        CommunityPostVO vo = communityService.getDetail(id);
        // 如果已登录，检查是否点赞
        if (auth != null) {
            // TODO: check isLiked
        }
        return Result.success(vo);
    }

    @PostMapping("/posts")
    public Result<CommunityPostVO> create(Authentication auth, @RequestBody CommunityPost post,
                                           @RequestParam(required = false) List<String> images) {
        Long userId = (Long) auth.getPrincipal();
        String role = auth.getAuthorities().stream()
                .map(Object::toString).filter(r -> r.startsWith("ROLE_"))
                .findFirst().orElse("ROLE_USER").replace("ROLE_", "");
        return Result.success(communityService.create(userId, role, post, images));
    }

    @PutMapping("/posts/{id}")
    public Result<CommunityPostVO> update(@PathVariable Long id, Authentication auth,
                                           @RequestBody CommunityPost post) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(communityService.update(id, userId, post));
    }

    @DeleteMapping("/posts/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        communityService.delete(id, userId);
        return Result.success();
    }

    @PostMapping("/posts/{id}/like")
    public Result<Integer> toggleLike(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(communityService.toggleLike(id, userId));
    }

    @GetMapping("/comments/{postId}")
    public Result<List<Map<String, Object>>> comments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(communityService.getComments(postId, page, size));
    }

    @PostMapping("/comments")
    public Result<Map<String, Object>> addComment(Authentication auth, @RequestBody Map<String, Object> body) {
        Long userId = (Long) auth.getPrincipal();
        Long postId = Long.valueOf(body.get("postId").toString());
        String content = (String) body.get("content");
        Long parentId = body.containsKey("parentId") && body.get("parentId") != null ?
                Long.valueOf(body.get("parentId").toString()) : null;
        return Result.success(communityService.addComment(postId, userId, content, parentId));
    }

    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        communityService.deleteComment(id, userId);
        return Result.success();
    }

    // ===== 管理员接口 =====

    @GetMapping("/admin/posts")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return Result.success(communityService.queryAllPosts(page, size, status));
    }

    @PutMapping("/admin/posts/{id}/review")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public Result<String> review(@PathVariable Long id, @RequestBody Map<String, String> body) {
        communityService.reviewPost(id, body.get("status"));
        return Result.success("审核完成");
    }
}
