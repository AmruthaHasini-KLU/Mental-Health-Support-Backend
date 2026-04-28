package com.healthsupport.service;

import com.healthsupport.dto.CommentRequest;
import com.healthsupport.model.Comment;
import com.healthsupport.model.Post;
import com.healthsupport.model.User;
import com.healthsupport.repository.CommentRepository;
import com.healthsupport.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserService userService;

    public Comment addComment(CommentRequest request, String email) {
        User user = userService.getCurrentUserEntity(email);
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .user(user)
                .build();

        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByPost(Long postId) {
        return commentRepository.findByPostIdOrderByTimestampAsc(postId);
    }
}
