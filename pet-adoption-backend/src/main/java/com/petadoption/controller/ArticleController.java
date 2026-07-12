package com.petadoption.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petadoption.entity.Article;
import com.petadoption.entity.ArticleComment;
import com.petadoption.entity.ArticleTag;
import com.petadoption.exception.Result;
import com.petadoption.mapper.ArticleTagMapper;
import com.petadoption.service.ArticleService;
import com.petadoption.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleTagMapper tagMapper;

    /** 公开：文章列表（仅已发布） */
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag) {
        return Result.success(articleService.queryArticles(page, size, category, keyword, tag));
    }

    @GetMapping("/{id}")
    public Result<ArticleVO> detail(@PathVariable Long id) {
        return Result.success(articleService.getDetail(id));
    }

    /** 任何登录用户均可发布文章，普通用户需审核 */
    @PostMapping
    public Result<ArticleVO> create(Authentication auth, @RequestBody Article article,
                                     @RequestParam(required = false) List<Long> tagIds) {
        Long userId = (Long) auth.getPrincipal();
        String role = auth.getAuthorities().stream()
                .map(Object::toString).filter(r -> r.startsWith("ROLE_"))
                .findFirst().orElse("ROLE_USER").replace("ROLE_", "");
        article.setAuthorId(userId);
        // 普通用户待审核，管理员/机构自动发布
        if (!"ADMIN".equals(role) && !"SHELTER".equals(role)) {
            article.setStatus("PENDING");
        } else {
            article.setStatus("PUBLISHED");
        }
        return Result.success(articleService.create(article, tagIds));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ArticleVO> update(@PathVariable Long id, @RequestBody Article article,
                                     @RequestParam(required = false) List<Long> tagIds) {
        return Result.success(articleService.update(id, article, tagIds));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }

    @GetMapping("/tags")
    public Result<List<Map<String, Object>>> tags() {
        return Result.success(articleService.getAllTags());
    }

    @PostMapping("/tags")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> createTag(@RequestBody Map<String, String> body) {
        ArticleTag tag = new ArticleTag();
        tag.setName(body.get("name"));
        tagMapper.insert(tag);
        return Result.success();
    }

    @GetMapping("/checklist")
    public Result<Map<String, Object>> checklist() {
        return Result.success(articleService.queryArticles(1, 1, "CHECKLIST", null, null));
    }

    // ===== 管理员接口 =====

    /** 管理员：文章列表（含待审核） */
    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return Result.success(articleService.queryAllArticles(page, size, status));
    }

    /** 管理员：审核文章 */
    @PutMapping("/admin/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> review(@PathVariable Long id, @RequestBody Map<String, String> body) {
        articleService.reviewArticle(id, body.get("status"));
        return Result.success("审核完成");
    }

    // ===== 评论接口 =====

    @GetMapping("/{articleId}/comments")
    public Result<Map<String, Object>> comments(@PathVariable Long articleId,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        Page<Map<String, Object>> result = articleService.getArticleComments(articleId, page, size);
        return Result.success(Map.of(
                "records", result.getRecords(),
                "total", result.getTotal(),
                "page", result.getCurrent(),
                "size", result.getSize(),
                "pages", result.getPages()
        ));
    }

    @PostMapping("/{articleId}/comments")
    public Result<ArticleComment> addComment(Authentication auth,
                                             @PathVariable Long articleId,
                                             @RequestBody Map<String, Object> body) {
        Long userId = (Long) auth.getPrincipal();
        String content = (String) body.get("content");
        Object parentIdObj = body.get("parentId");
        Long parentId = parentIdObj != null ? Long.valueOf(parentIdObj.toString()) : null;
        return Result.success(articleService.addArticleComment(userId, articleId, content, parentId));
    }

    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        articleService.deleteArticleComment(id, userId);
        return Result.success();
    }
}
