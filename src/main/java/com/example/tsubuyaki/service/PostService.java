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

    // Serviceは投稿に関する業務ルールをまとめ、Repositoryへの保存・検索を調整する。
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
        // 一覧では論理削除済みを除いた最新投稿だけを取得する。
        return repository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    public List<Post> searchByBody(String keyword) {
        // 検索結果も一覧と同じく論理削除済みを除外する。
        return repository.findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc(keyword);
    }

    @Transactional
    public Post create(String author, String body, String avatarColor) {
        // 投稿保存後に本文中のタグを解析し、投稿とタグを紐づける。
        Post post = new Post(author, body, avatarColor, Instant.now());
        post = repository.save(post);
        saveTags(post, body);
        return post;
    }

    @Transactional
    public Post create(String author, String body, String avatarColor,
            String imageContentType, byte[] imageData) {
        // 画像付き投稿は本文投稿と同じ流れで保存し、画像だけEntityへ持たせる。
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
        // タグ別一覧はRepositoryの結合検索に委譲する。
        return repository.findByTagNameOrderByCreatedAtDesc(name);
    }

    @Transactional
    public void delete(Long id) {
        // 物理削除せず、deleted_atだけを設定して一覧から除外する。
        Post post = repository.findById(id)
                .orElseThrow(PostNotFoundException::new);
        post.markDeleted(Instant.now());
    }

    private void saveTags(Post post, String body) {
        // 本文から抽出したタグを再利用または新規作成し、投稿との中間テーブルへ保存する。
        for (String tagName : extractTagNames(body)) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(new Tag(tagName)));
            postTagRepository.save(new PostTag(post, tag));
        }
    }

    private static Set<String> extractTagNames(String body) {
        // 同一投稿内の重複タグは1回だけ保存する。
        Set<String> tagNames = new LinkedHashSet<>();
        Matcher matcher = TAG_PATTERN.matcher(body);
        while (matcher.find()) {
            tagNames.add(matcher.group(1));
        }
        return tagNames;
    }
}
