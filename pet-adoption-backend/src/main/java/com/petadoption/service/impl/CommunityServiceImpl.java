package com.petadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.entity.*;
import com.petadoption.enums.CommunityPostTypeEnum;
import com.petadoption.exception.BusinessException;
import com.petadoption.mapper.*;
import com.petadoption.service.CommunityService;
import com.petadoption.vo.CommunityPostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final CommunityPostMapper postMapper;
    private final CommunityImageMapper imageMapper;
    private final CommunityLikeMapper likeMapper;
    private final CommunityCommentMapper commentMapper;
    private final UserMapper userMapper;

    @Override
    public Map<String, Object> queryPosts(int page, int size, String type, String sortBy) {
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<CommunityPost>()
                .eq(CommunityPost::getStatus, "PUBLISHED");
        if (type != null && !type.isEmpty()) wrapper.eq(CommunityPost::getType, type);
        if ("popular".equals(sortBy)) wrapper.orderByDesc(CommunityPost::getLikeCount);
        else wrapper.orderByDesc(CommunityPost::getCreatedAt);

        Page<CommunityPost> pageObj = new Page<>(page, size);
        postMapper.selectPage(pageObj, wrapper);
        List<CommunityPostVO> vos = pageObj.getRecords().stream().map(this::toPostVO).collect(Collectors.toList());
        return Map.of("records", vos, "total", pageObj.getTotal(), "page", page, "size", size);
    }

    @Override
    public CommunityPostVO getDetail(Long id) {
        CommunityPost post = postMapper.selectById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        post.setViewCount((post.getViewCount() != null ? post.getViewCount() : 0) + 1);
        postMapper.updateById(post);
        return toPostVO(post);
    }

    @Override
    @Transactional
    public CommunityPostVO create(Long userId, String userRole, CommunityPost post, List<String> images) {
        post.setUserId(userId);
        // ADMIN/SHELTER 发帖自动通过，普通用户需审核
        if ("ADMIN".equals(userRole) || "SHELTER".equals(userRole)) {
            post.setStatus("PUBLISHED");
        } else {
            post.setStatus("PENDING");
        }
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        postMapper.insert(post);
        if (images != null) {
            for (int i = 0; i < images.size(); i++) {
                CommunityImage img = new CommunityImage();
                img.setPostId(post.getId());
                img.setUrl(images.get(i));
                img.setSortOrder(i);
                imageMapper.insert(img);
            }
        }
        return toPostVO(post);
    }

    @Override
    @Transactional
    public CommunityPostVO update(Long id, Long userId, CommunityPost updated) {
        CommunityPost post = postMapper.selectById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        if (!post.getUserId().equals(userId)) throw new BusinessException("只能修改自己的帖子");
        post.setTitle(updated.getTitle());
        post.setContent(updated.getContent());
        post.setType(updated.getType());
        postMapper.updateById(post);
        return toPostVO(post);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        CommunityPost post = postMapper.selectById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        if (!post.getUserId().equals(userId)) throw new BusinessException("只能删除自己的帖子");
        imageMapper.delete(new LambdaQueryWrapper<CommunityImage>().eq(CommunityImage::getPostId, id));
        commentMapper.delete(new LambdaQueryWrapper<CommunityComment>().eq(CommunityComment::getPostId, id));
        likeMapper.delete(new LambdaQueryWrapper<CommunityLike>().eq(CommunityLike::getPostId, id));
        postMapper.deleteById(id);
    }

    @Override
    @Transactional
    public int toggleLike(Long postId, Long userId) {
        CommunityLike existing = likeMapper.selectOne(new LambdaQueryWrapper<CommunityLike>()
                .eq(CommunityLike::getPostId, postId).eq(CommunityLike::getUserId, userId));
        CommunityPost post = postMapper.selectById(postId);
        if (existing != null) {
            likeMapper.deleteById(existing.getId());
            post.setLikeCount(Math.max(0, (post.getLikeCount() != null ? post.getLikeCount() : 1) - 1));
            postMapper.updateById(post);
            return post.getLikeCount();
        } else {
            CommunityLike like = new CommunityLike();
            like.setPostId(postId);
            like.setUserId(userId);
            likeMapper.insert(like);
            post.setLikeCount((post.getLikeCount() != null ? post.getLikeCount() : 0) + 1);
            postMapper.updateById(post);
            return post.getLikeCount();
        }
    }

    @Override
    public List<Map<String, Object>> getComments(Long postId, int page, int size) {
        Page<CommunityComment> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<CommunityComment> wrapper = new LambdaQueryWrapper<CommunityComment>()
                .eq(CommunityComment::getPostId, postId)
                .isNull(CommunityComment::getParentId)
                .orderByDesc(CommunityComment::getCreatedAt);
        commentMapper.selectPage(pageObj, wrapper);

        return pageObj.getRecords().stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("userId", c.getUserId());
            m.put("content", c.getContent());
            m.put("createdAt", c.getCreatedAt());
            User u = userMapper.selectById(c.getUserId());
            m.put("username", u != null ? (u.getNickname() != null ? u.getNickname() : u.getUsername()) : "");
            m.put("avatar", u != null ? u.getAvatar() : "");
            // replies
            List<CommunityComment> replies = commentMapper.selectList(
                new LambdaQueryWrapper<CommunityComment>().eq(CommunityComment::getParentId, c.getId())
                    .orderByAsc(CommunityComment::getCreatedAt));
            m.put("replies", replies.stream().map(r -> {
                Map<String, Object> rm = new HashMap<>();
                rm.put("id", r.getId());
                rm.put("userId", r.getUserId());
                rm.put("content", r.getContent());
                rm.put("createdAt", r.getCreatedAt());
                User ru = userMapper.selectById(r.getUserId());
                rm.put("username", ru != null ? (ru.getNickname() != null ? ru.getNickname() : ru.getUsername()) : "");
                rm.put("avatar", ru != null ? ru.getAvatar() : "");
                return rm;
            }).collect(Collectors.toList()));
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> addComment(Long postId, Long userId, String content, Long parentId) {
        CommunityComment comment = new CommunityComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId);
        commentMapper.insert(comment);
        // 更新评论数
        CommunityPost post = postMapper.selectById(postId);
        post.setCommentCount((post.getCommentCount() != null ? post.getCommentCount() : 0) + 1);
        postMapper.updateById(post);

        Map<String, Object> m = new HashMap<>();
        m.put("id", comment.getId());
        m.put("content", comment.getContent());
        m.put("createdAt", comment.getCreatedAt());
        User u = userMapper.selectById(userId);
        m.put("username", u != null ? (u.getNickname() != null ? u.getNickname() : u.getUsername()) : "");
        m.put("avatar", u != null ? u.getAvatar() : "");
        return m;
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        CommunityComment comment = commentMapper.selectById(commentId);
        if (comment == null) throw new BusinessException("评论不存在");
        if (!comment.getUserId().equals(userId)) throw new BusinessException("只能删除自己的评论");
        commentMapper.deleteById(commentId);
    }

    // ===== 管理员方法 =====

    @Override
    public Map<String, Object> queryAllPosts(int page, int size, String status) {
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(CommunityPost::getStatus, status);
        }
        wrapper.orderByDesc(CommunityPost::getCreatedAt);
        Page<CommunityPost> pageObj = new Page<>(page, size);
        postMapper.selectPage(pageObj, wrapper);
        List<CommunityPostVO> vos = pageObj.getRecords().stream().map(this::toPostVO).collect(Collectors.toList());
        return Map.of("records", vos, "total", pageObj.getTotal(), "page", page, "size", size);
    }

    @Override
    @Transactional
    public void reviewPost(Long id, String status) {
        CommunityPost post = postMapper.selectById(id);
        if (post == null) throw new BusinessException("帖子不存在");
        if (!"PUBLISHED".equals(status) && !"BLOCKED".equals(status))
            throw new BusinessException("无效状态，请使用 PUBLISHED 或 BLOCKED");
        post.setStatus(status);
        postMapper.updateById(post);
    }

    private CommunityPostVO toPostVO(CommunityPost post) {
        CommunityPostVO vo = new CommunityPostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setType(post.getType());
        vo.setTypeName(CommunityPostTypeEnum.getDesc(post.getType()));
        vo.setStatus(post.getStatus());
        vo.setViewCount(post.getViewCount());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setCreatedAt(post.getCreatedAt());

        User u = userMapper.selectById(post.getUserId());
        if (u != null) {
            vo.setUsername(u.getNickname() != null ? u.getNickname() : u.getUsername());
            vo.setUserAvatar(u.getAvatar());
        }
        List<CommunityImage> images = imageMapper.selectList(
            new LambdaQueryWrapper<CommunityImage>().eq(CommunityImage::getPostId, post.getId())
                .orderByAsc(CommunityImage::getSortOrder));
        vo.setImages(images.stream().map(CommunityImage::getUrl).collect(Collectors.toList()));
        return vo;
    }
}
