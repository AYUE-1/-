package com.petadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.entity.Comment;
import com.petadoption.entity.User;
import com.petadoption.exception.BusinessException;
import com.petadoption.mapper.CommentMapper;
import com.petadoption.mapper.UserMapper;
import com.petadoption.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    @Override
    public Page<Map<String, Object>> getPetComments(Long petId, int page, int size) {
        Page<Comment> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPetId, petId)
                .isNull(Comment::getParentId)
                .orderByDesc(Comment::getCreatedAt);
        commentMapper.selectPage(pageObj, wrapper);

        Page<Map<String, Object>> resultPage = new Page<>(page, size, pageObj.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();

        for (Comment comment : pageObj.getRecords()) {
            Map<String, Object> map = buildCommentMap(comment);
            // 获取回复列表
            List<Comment> replies = commentMapper.selectList(
                    new LambdaQueryWrapper<Comment>()
                            .eq(Comment::getParentId, comment.getId())
                            .orderByAsc(Comment::getCreatedAt)
            );
            List<Map<String, Object>> replyMaps = new ArrayList<>();
            for (Comment reply : replies) {
                replyMaps.add(buildCommentMap(reply));
            }
            map.put("replies", replyMaps);
            records.add(map);
        }

        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public Comment addComment(Long userId, Long petId, String content, Long parentId) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setPetId(petId);
        comment.setContent(content);
        comment.setParentId(parentId);
        commentMapper.insert(comment);
        return comment;
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此评论");
        }
        // 删除该评论及其所有回复
        commentMapper.deleteById(commentId);
        commentMapper.delete(new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, commentId));
    }

    private Map<String, Object> buildCommentMap(Comment comment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", comment.getId());
        map.put("petId", comment.getPetId());
        map.put("userId", comment.getUserId());
        map.put("content", comment.getContent());
        map.put("parentId", comment.getParentId());
        map.put("createdAt", comment.getCreatedAt());

        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            map.put("username", user.getUsername());
            map.put("nickname", user.getNickname());
            map.put("avatar", user.getAvatar());
        }
        return map;
    }
}
