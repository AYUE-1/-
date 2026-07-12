package com.petadoption.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("t_article_tag")
public class ArticleTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
}
