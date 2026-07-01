package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
@Import(LikeService.class)
class LikeServiceIntegrationTest {

    @Autowired
    private LikeService likeService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Test
    @DisplayName("いいね_同一clientHashでトグルするとカウントが増減する")
    void いいね_同一clientHashでトグルするとカウントが増減する() {
        Post post = postRepository.save(new Post(
                "alice", "本文です", Instant.parse("2026-06-30T10:15:00Z")));
        Long postId = post.getId();

        assertThat(postLikeRepository.countByPostId(postId)).isZero();

        likeService.toggle(postId, "client01");

        assertThat(postLikeRepository.countByPostId(postId)).isEqualTo(1L);

        likeService.toggle(postId, "client01");

        assertThat(postLikeRepository.countByPostId(postId)).isZero();
    }
}
