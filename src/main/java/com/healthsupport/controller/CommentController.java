package com.healthsupport.controller;

import com.healthsupport.dto.CommentRequest;
import com.healthsupport.model.Comment;
import com.healthsupport.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Comment> addComment(@RequestBody CommentRequest request, Authentication authentication) {
        return ResponseEntity.ok(commentService.addComment(request, authentication.getName()));
    }

    @GetMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }
}
