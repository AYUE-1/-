package com.petadoption.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleVO {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private String category;
    private String categoryName;
    private String status;
    private Long authorId;
    private String authorName;
    private Integer viewCount;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
