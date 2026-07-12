package com.petadoption.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.entity.Comment;

import java.util.Map;

public interface CommentService {
    Page<Map<String, Object>> getPetComments(Long petId, int page, int size);
    Comment addComment(Long userId, Long petId, String content, Long parentId);
    void deleteComment(Long commentId, Long userId);
}
