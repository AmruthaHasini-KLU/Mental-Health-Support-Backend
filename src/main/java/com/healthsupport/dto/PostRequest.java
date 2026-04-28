package com.healthsupport.dto;

import lombok.Data;

@Data
public class PostRequest {
    private String content;
    private boolean anonymous;
}
