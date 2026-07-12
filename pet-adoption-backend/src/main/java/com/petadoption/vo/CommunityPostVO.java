package com.petadoption.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommunityPostVO {
    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;
    private String title;
    private String content;
    private String type;
    private String typeName;
    private String status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isLiked;
    private List<String> images;
    private LocalDateTime createdAt;
}
