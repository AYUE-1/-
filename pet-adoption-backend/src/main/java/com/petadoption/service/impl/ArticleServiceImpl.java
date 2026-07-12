package com.petadoption.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.entity.*;
import com.petadoption.enums.ArticleCategoryEnum;
import com.petadoption.exception.BusinessException;
import com.petadoption.mapper.*;
import com.petadoption.service.ArticleService;
import com.petadoption.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper tagMapper;
    private final ArticleTagRelMapper tagRelMapper;
    private final ArticleCommentMapper commentMapper;
    private final UserMapper userMapper;

    @Override
    public Map<String, Object> queryArticles(int page, int size, String category, String keyword, String tag) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, "PUBLISHED");
        if (StringUtils.hasText(category)) wrapper.eq(Article::getCategory, category);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Article::getTitle, keyword).or().like(Article::getSummary, keyword));
        }
        wrapper.orderByDesc(Article::getCreatedAt);

        Page<Article> pageObj = new Page<>(page, size);
        articleMapper.selectPage(pageObj, wrapper);

        List<ArticleVO> vos = pageObj.getRecords().stream().map(this::toArticleVO).collect(Collectors.toList());
        // 如果有标签筛选
        if (StringUtils.hasText(tag)) {
            ArticleTag tagEntity = tagMapper.selectOne(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getName, tag));
            if (tagEntity != null) {
                List<ArticleTagRel> rels = tagRelMapper.selectList(
                    new LambdaQueryWrapper<ArticleTagRel>().eq(ArticleTagRel::getTagId, tagEntity.getId()));
                Set<Long> articleIds = rels.stream().map(ArticleTagRel::getArticleId).collect(Collectors.toSet());
                vos = vos.stream().filter(v -> articleIds.contains(v.getId())).collect(Collectors.toList());
            }
        }

        return Map.of("records", vos, "total", pageObj.getTotal(), "page", page, "size", size);
    }

    @Override
    public ArticleVO getDetail(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) throw new BusinessException("文章不存在");
        article.setViewCount((article.getViewCount() != null ? article.getViewCount() : 0) + 1);
        articleMapper.updateById(article);
        return toArticleVO(article);
    }

    @Override
    @Transactional
    public ArticleVO create(Article article, List<Long> tagIds) {
        articleMapper.insert(article);
        updateTags(article.getId(), tagIds);
        return toArticleVO(article);
    }

    @Override
    @Transactional
    public ArticleVO update(Long id, Article article, List<Long> tagIds) {
        Article existing = articleMapper.selectById(id);
        if (existing == null) throw new BusinessException("文章不存在");
        existing.setTitle(article.getTitle());
        existing.setContent(article.getContent());
        existing.setSummary(article.getSummary());
        existing.setCoverImage(article.getCoverImage());
        existing.setCategory(article.getCategory());
        articleMapper.updateById(existing);
        updateTags(id, tagIds);
        return toArticleVO(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        tagRelMapper.delete(new LambdaQueryWrapper<ArticleTagRel>().eq(ArticleTagRel::getArticleId, id));
        articleMapper.deleteById(id);
    }

    @Override
    public List<Map<String, Object>> getAllTags() {
        return tagMapper.selectList(null).stream()
            .map(t -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", t.getId());
                m.put("name", t.getName());
                return m;
            }).collect(Collectors.toList());
    }

    // ===== 管理员方法 =====

    @Override
    public Map<String, Object> queryAllArticles(int page, int size, String status) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Article::getStatus, status);
        }
        wrapper.orderByDesc(Article::getCreatedAt);
        Page<Article> pageObj = new Page<>(page, size);
        articleMapper.selectPage(pageObj, wrapper);
        List<ArticleVO> vos = pageObj.getRecords().stream().map(this::toArticleVO).collect(Collectors.toList());
        return Map.of("records", vos, "total", pageObj.getTotal(), "page", page, "size", size);
    }

    @Override
    @Transactional
    public void reviewArticle(Long id, String status) {
        Article article = articleMapper.selectById(id);
        if (article == null) throw new BusinessException("文章不存在");
        if (!"PUBLISHED".equals(status) && !"BLOCKED".equals(status))
            throw new BusinessException("无效状态，请使用 PUBLISHED 或 BLOCKED");
        article.setStatus(status);
        articleMapper.updateById(article);
    }

    private void updateTags(Long articleId, List<Long> tagIds) {
        tagRelMapper.delete(new LambdaQueryWrapper<ArticleTagRel>().eq(ArticleTagRel::getArticleId, articleId));
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                ArticleTagRel rel = new ArticleTagRel();
                rel.setArticleId(articleId);
                rel.setTagId(tagId);
                tagRelMapper.insert(rel);
            }
        }
    }

    private ArticleVO toArticleVO(Article article) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setContent(article.getContent());
        vo.setSummary(article.getSummary());
        vo.setCoverImage(article.getCoverImage());
        vo.setCategory(article.getCategory());
        vo.setCategoryName(ArticleCategoryEnum.getDesc(article.getCategory()));
        vo.setStatus(article.getStatus());
        vo.setAuthorId(article.getAuthorId());
        vo.setViewCount(article.getViewCount());
        vo.setCreatedAt(article.getCreatedAt());
        vo.setUpdatedAt(article.getUpdatedAt());

        if (article.getAuthorId() != null) {
            User author = userMapper.selectById(article.getAuthorId());
            if (author != null) vo.setAuthorName(author.getNickname());
        }

        // 标签
        List<ArticleTagRel> rels = tagRelMapper.selectList(
            new LambdaQueryWrapper<ArticleTagRel>().eq(ArticleTagRel::getArticleId, article.getId()));
        if (!rels.isEmpty()) {
            Set<Long> tagIds = rels.stream().map(ArticleTagRel::getTagId).collect(Collectors.toSet());
            List<ArticleTag> tags = tagMapper.selectBatchIds(tagIds);
            vo.setTags(tags.stream().map(ArticleTag::getName).collect(Collectors.toList()));
        }
        return vo;
    }

    // ===== 评论功能 =====

    @Override
    public Page<Map<String, Object>> getArticleComments(Long articleId, int page, int size) {
        Page<ArticleComment> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<ArticleComment> wrapper = new LambdaQueryWrapper<ArticleComment>()
                .eq(ArticleComment::getArticleId, articleId)
                .isNull(ArticleComment::getParentId)
                .orderByDesc(ArticleComment::getCreatedAt);
        commentMapper.selectPage(pageObj, wrapper);

        Page<Map<String, Object>> resultPage = new Page<>(page, size, pageObj.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();

        for (ArticleComment comment : pageObj.getRecords()) {
            Map<String, Object> map = buildArticleCommentMap(comment);
            List<ArticleComment> replies = commentMapper.selectList(
                    new LambdaQueryWrapper<ArticleComment>()
                            .eq(ArticleComment::getParentId, comment.getId())
                            .orderByAsc(ArticleComment::getCreatedAt));
            List<Map<String, Object>> replyMaps = new ArrayList<>();
            for (ArticleComment reply : replies) {
                replyMaps.add(buildArticleCommentMap(reply));
            }
            map.put("replies", replyMaps);
            records.add(map);
        }

        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public ArticleComment addArticleComment(Long userId, Long articleId, String content, Long parentId) {
        ArticleComment comment = new ArticleComment();
        comment.setUserId(userId);
        comment.setArticleId(articleId);
        comment.setContent(content);
        comment.setParentId(parentId);
        commentMapper.insert(comment);
        return comment;
    }

    @Override
    public void deleteArticleComment(Long commentId, Long userId) {
        ArticleComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此评论");
        }
        commentMapper.deleteById(commentId);
        commentMapper.delete(new LambdaQueryWrapper<ArticleComment>().eq(ArticleComment::getParentId, commentId));
    }

    private Map<String, Object> buildArticleCommentMap(ArticleComment comment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", comment.getId());
        map.put("articleId", comment.getArticleId());
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
