package com.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("t_article_tag_rel")
public class ArticleTagRel {
    private Long articleId;
    private Long tagId;
}
