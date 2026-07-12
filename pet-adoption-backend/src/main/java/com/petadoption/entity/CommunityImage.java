package com.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("t_community_image")
public class CommunityImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private String url;
    private Integer sortOrder;
}
