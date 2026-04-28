package com.healthsupport.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private Long postId;
    private String content;
}
