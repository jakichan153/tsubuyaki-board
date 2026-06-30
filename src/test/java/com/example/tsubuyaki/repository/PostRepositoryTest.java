package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("h2")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("投稿一覧_51件以上あるとき_新着50件だけを新着順で返す")
    void 投稿一覧_51件以上あるとき_新着50件だけを新着順で返す() {
        Instant base = Instant.parse("2026-06-30T00:00:00Z");
        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= 51; i++) {
            posts.add(new Post("author-" + i, "body-" + i, base.plusSeconds(i)));
        }
        postRepository.saveAll(posts);

        List<Post> actual = postRepository.findTop50ByOrderByCreatedAtDesc();

        assertThat(actual).hasSize(50);
        assertThat(actual).extracting(Post::getAuthor)
                .startsWith("author-51", "author-50", "author-49")
                .endsWith("author-4", "author-3", "author-2")
                .doesNotContain("author-1");
    }
}
