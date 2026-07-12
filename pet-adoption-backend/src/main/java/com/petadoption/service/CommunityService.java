package com.petadoption.service;

import com.petadoption.entity.CommunityPost;
import com.petadoption.vo.CommunityPostVO;

import java.util.List;
import java.util.Map;

public interface CommunityService {
    Map<String, Object> queryPosts(int page, int size, String type, String sortBy);
    CommunityPostVO getDetail(Long id);
    CommunityPostVO create(Long userId, String userRole, CommunityPost post, List<String> images);
    CommunityPostVO update(Long id, Long userId, CommunityPost post);
    void delete(Long id, Long userId);
    int toggleLike(Long postId, Long userId);
    List<Map<String, Object>> getComments(Long postId, int page, int size);
    Map<String, Object> addComment(Long postId, Long userId, String content, Long parentId);
    void deleteComment(Long commentId, Long userId);
    // Admin
    Map<String, Object> queryAllPosts(int page, int size, String status);
    void reviewPost(Long id, String status);
}
