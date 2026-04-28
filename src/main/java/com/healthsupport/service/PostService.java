package com.healthsupport.service;

import com.healthsupport.dto.PostRequest;
import com.healthsupport.dto.PostResponse;
import com.healthsupport.model.Post;
import com.healthsupport.model.User;
import com.healthsupport.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;

    public PostResponse createPost(PostRequest request, String email) {
        User user = userService.getCurrentUserEntity(email);

        Post post = Post.builder()
                .content(request.getContent())
                .anonymous(request.isAnonymous())
                .user(user)
                .build();

        post = postRepository.save(post);
        return mapToResponse(post);
    }

    public List<PostResponse> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PostResponse mapToResponse(Post post) {
        String displayName = post.isAnonymous() ? "Anonymous" : post.getUser().getName();

        return PostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .userName(displayName)
                .anonymous(post.isAnonymous())
                .build();
    }

    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new IllegalArgumentException("Post not found");
        }
        postRepository.deleteById(id);
    }
}
