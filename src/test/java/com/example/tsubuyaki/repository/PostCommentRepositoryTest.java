package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostComment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostCommentRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Test
    @DisplayName("コメント一覧_投稿idに紐づくコメントを新しい順で返す")
    void コメント一覧_投稿idに紐づくコメントを新しい順で返す() {
        Post post = postRepository.save(new Post("alice", "本文です", Instant.parse("2026-06-30T10:00:00Z")));
        postCommentRepository.save(new PostComment(
                post, "bob", "古いコメント", Instant.parse("2026-06-30T11:00:00Z")));
        postCommentRepository.save(new PostComment(
                post, "carol", "新しいコメント", Instant.parse("2026-06-30T12:00:00Z")));

        List<PostComment> actual = postCommentRepository.findByPostIdOrderByCreatedAtDesc(post.getId());

        assertThat(actual).extracting(PostComment::getBody)
                .containsExactly("新しいコメント", "古いコメント");
    }

    @Test
    @DisplayName("コメント数_投稿idに紐づくコメント件数を返す")
    void コメント数_投稿idに紐づくコメント件数を返す() {
        Post post = postRepository.save(new Post("alice", "本文です", Instant.parse("2026-06-30T10:00:00Z")));
        postCommentRepository.save(new PostComment(
                post, "bob", "コメント1", Instant.parse("2026-06-30T11:00:00Z")));
        postCommentRepository.save(new PostComment(
                post, "carol", "コメント2", Instant.parse("2026-06-30T12:00:00Z")));

        long actual = postCommentRepository.countByPostId(post.getId());

        assertThat(actual).isEqualTo(2L);
    }
}
