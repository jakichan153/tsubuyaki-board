package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;

    public PostService(PostRepository repository) {
        this.repository = repository;
    }

    public List<Post> latest() {
        return repository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    public List<Post> searchByBody(String keyword) {
        return repository.findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc(keyword);
    }

    @Transactional
    public Post create(String author, String body, String avatarColor) {
        return repository.save(new Post(author, body, avatarColor, Instant.now()));
    }

    public Optional<Post> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public void delete(Long id) {
        Post post = repository.findById(id)
                .orElseThrow(PostNotFoundException::new);
        post.markDeleted(Instant.now());
    }
}
