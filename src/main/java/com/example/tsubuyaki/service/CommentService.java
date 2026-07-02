package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostComment;
import com.example.tsubuyaki.repository.PostCommentRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;

    public CommentService(PostRepository postRepository, PostCommentRepository postCommentRepository) {
        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
    }

    @Transactional
    public PostComment create(Long postId, String author, String body) {
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        return postCommentRepository.save(new PostComment(post, author, body, Instant.now()));
    }

    public List<PostComment> latestByPostId(Long postId) {
        return postCommentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }

    public long countByPostId(Long postId) {
        return postCommentRepository.countByPostId(postId);
    }
}
