package com.petadoption.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.entity.Article;
import com.petadoption.entity.ArticleComment;
import com.petadoption.vo.ArticleVO;

import java.util.List;
import java.util.Map;

public interface ArticleService {
    Map<String, Object> queryArticles(int page, int size, String category, String keyword, String tag);
    ArticleVO getDetail(Long id);
    ArticleVO create(Article article, List<Long> tagIds);
    ArticleVO update(Long id, Article article, List<Long> tagIds);
    void delete(Long id);
    List<Map<String, Object>> getAllTags();
    // Admin
    Map<String, Object> queryAllArticles(int page, int size, String status);
    void reviewArticle(Long id, String status);
    // Comments
    Page<Map<String, Object>> getArticleComments(Long articleId, int page, int size);
    ArticleComment addArticleComment(Long userId, Long articleId, String content, Long parentId);
    void deleteArticleComment(Long commentId, Long userId);
}
