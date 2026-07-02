package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostTag;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.PostTagRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class PostService {

    private static final Pattern TAG_PATTERN = Pattern.compile("#([\\p{L}\\p{N}_]+)");

    private final PostRepository repository;
    private final TagRepository tagRepository;
    private final PostTagRepository postTagRepository;

    public PostService(PostRepository repository, TagRepository tagRepository,
            PostTagRepository postTagRepository) {
        this.repository = repository;
        this.tagRepository = tagRepository;
        this.postTagRepository = postTagRepository;
    }

    public List<Post> latest() {
        return repository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    public List<Post> searchByBody(String keyword) {
        return repository.findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc(keyword);
    }

    @Transactional
    public Post create(String author, String body, String avatarColor) {
        Post post = new Post(author, body, avatarColor, Instant.now());
        post = repository.save(post);
        saveTags(post, body);
        return post;
    }

    @Transactional
    public Post create(String author, String body, String avatarColor,
            String imageContentType, byte[] imageData) {
        Post post = new Post(author, body, avatarColor, Instant.now());
        post.attachImage(imageContentType, imageData);
        post = repository.save(post);
        saveTags(post, body);
        return post;
    }

    public Optional<Post> findById(Long id) {
        return repository.findById(id);
    }

    public List<Post> findByTagName(String name) {
        return repository.findByTagNameOrderByCreatedAtDesc(name);
    }

    @Transactional
    public void delete(Long id) {
        Post post = repository.findById(id)
                .orElseThrow(PostNotFoundException::new);
        post.markDeleted(Instant.now());
    }

    private void saveTags(Post post, String body) {
        for (String tagName : extractTagNames(body)) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(new Tag(tagName)));
            postTagRepository.save(new PostTag(post, tag));
        }
    }

    private static Set<String> extractTagNames(String body) {
        Set<String> tagNames = new LinkedHashSet<>();
        Matcher matcher = TAG_PATTERN.matcher(body);
        while (matcher.find()) {
            tagNames.add(matcher.group(1));
        }
        return tagNames;
    }
}
