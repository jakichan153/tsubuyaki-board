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

        List<Post> actual = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

        assertThat(actual).hasSize(50);
        assertThat(actual).extracting(Post::getAuthor)
                .startsWith("author-51", "author-50", "author-49")
                .endsWith("author-4", "author-3", "author-2")
                .doesNotContain("author-1");
    }

    @Test
    @DisplayName("投稿検索_本文にキーワードを含む投稿のみ新着順で返す")
    void 投稿検索_本文にキーワードを含む投稿のみ新着順で返す() {
        postRepository.save(new Post("alice", "検索できます", Instant.parse("2026-06-30T10:00:00Z")));
        postRepository.save(new Post("bob", "対象外です", Instant.parse("2026-06-30T11:00:00Z")));
        postRepository.save(new Post("carol", "新しい検索結果", Instant.parse("2026-06-30T12:00:00Z")));

        List<Post> actual = postRepository.findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc("検索");

        assertThat(actual).extracting(Post::getAuthor)
                .containsExactly("carol", "alice");
    }

    @Test
    @DisplayName("投稿検索_一致しない場合_空配列を返す")
    void 投稿検索_一致しない場合_空配列を返す() {
        postRepository.save(new Post("alice", "検索できます", Instant.parse("2026-06-30T10:00:00Z")));

        List<Post> actual = postRepository.findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc("該当なし");

        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("投稿作成_アバター色を保存できる")
    void 投稿作成_アバター色を保存できる() {
        Post saved = postRepository.save(new Post(
                "alice", "本文です", "green", Instant.parse("2026-06-30T10:00:00Z")));

        Post actual = postRepository.findById(saved.getId()).orElseThrow();

        assertThat(actual.getAvatarColor()).isEqualTo("green");
    }

    @Test
    @DisplayName("投稿一覧_論理削除済み投稿は表示しない")
    void 投稿一覧_論理削除済み投稿は表示しない() {
        Post deleted = new Post("alice", "削除済みです", Instant.parse("2026-06-30T10:00:00Z"));
        deleted.markDeleted(Instant.parse("2026-06-30T11:00:00Z"));
        postRepository.save(deleted);
        postRepository.save(new Post("bob", "表示されます", Instant.parse("2026-06-30T09:00:00Z")));

        List<Post> actual = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

        assertThat(actual).extracting(Post::getAuthor)
                .containsExactly("bob");
    }

    @Test
    @DisplayName("投稿一覧_論理削除されていない投稿は従来どおり表示する")
    void 投稿一覧_論理削除されていない投稿は従来どおり表示する() {
        postRepository.save(new Post("alice", "古い投稿です", Instant.parse("2026-06-30T09:00:00Z")));
        postRepository.save(new Post("bob", "新しい投稿です", Instant.parse("2026-06-30T10:00:00Z")));

        List<Post> actual = postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

        assertThat(actual).extracting(Post::getAuthor)
                .containsExactly("bob", "alice");
    }
}
