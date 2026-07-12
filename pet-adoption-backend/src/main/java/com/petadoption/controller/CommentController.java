package com.petadoption.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.entity.Comment;
import com.petadoption.exception.Result;
import com.petadoption.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/api/pets/{petId}/comments")
    public Result<Map<String, Object>> list(@PathVariable Long petId,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        Page<Map<String, Object>> result = commentService.getPetComments(petId, page, size);
        return Result.success(Map.of(
                "records", result.getRecords(),
                "total", result.getTotal(),
                "page", result.getCurrent(),
                "size", result.getSize(),
                "pages", result.getPages()
        ));
    }

    @PostMapping("/api/pets/{petId}/comments")
    public Result<Comment> add(Authentication auth,
                                @PathVariable Long petId,
                                @RequestBody Map<String, Object> body) {
        Long userId = (Long) auth.getPrincipal();
        String content = (String) body.get("content");
        Object parentIdObj = body.get("parentId");
        Long parentId = parentIdObj != null ? Long.valueOf(parentIdObj.toString()) : null;
        return Result.success(commentService.addComment(userId, petId, content, parentId));
    }

    @DeleteMapping("/api/comments/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        commentService.deleteComment(id, userId);
        return Result.success();
    }
}
