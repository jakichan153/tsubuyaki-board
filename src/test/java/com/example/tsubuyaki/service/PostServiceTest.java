package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("投稿一覧_latest_Repositoryの新着50件を返す")
    void 投稿一覧_latest_Repositoryの新着50件を返す() {
        List<Post> posts = List.of(
                new Post("alice", "新しい投稿", Instant.parse("2026-06-30T10:00:00Z")),
                new Post("bob", "古い投稿", Instant.parse("2026-06-30T09:00:00Z"))
        );
        given(postRepository.findTop50ByDeletedAtIsNullOrderByCreatedAtDesc()).willReturn(posts);

        List<Post> actual = postService.latest();

        assertThat(actual).isSameAs(posts);
        verify(postRepository).findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("投稿検索_searchByBody_Repositoryの本文LIKE検索結果を返す")
    void 投稿検索_searchByBody_Repositoryの本文LIKE検索結果を返す() {
        List<Post> posts = List.of(
                new Post("alice", "検索できます", Instant.parse("2026-06-30T10:00:00Z"))
        );
        given(postRepository.findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc("検索"))
                .willReturn(posts);

        List<Post> actual = postService.searchByBody("検索");

        assertThat(actual).isSameAs(posts);
        verify(postRepository).findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc("検索");
    }

    @Test
    @DisplayName("投稿作成_create_現在日時を設定してRepositoryに保存する")
    void 投稿作成_create_現在日時を設定してRepositoryに保存する() {
        Instant before = Instant.now();

        postService.create("alice", "初投稿です", "blue");

        Instant after = Instant.now();
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post saved = captor.getValue();
        assertThat(saved.getAuthor()).isEqualTo("alice");
        assertThat(saved.getBody()).isEqualTo("初投稿です");
        assertThat(saved.getAvatarColor()).isEqualTo("blue");
        assertThat(saved.getCreatedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("投稿詳細_findById_Repositoryの検索結果を返す")
    void 投稿詳細_findById_Repositoryの検索結果を返す() {
        Post post = new Post("alice", "詳細本文です", Instant.parse("2026-06-30T10:15:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        Optional<Post> actual = postService.findById(1L);

        assertThat(actual).containsSame(post);
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("投稿削除_delete_対象投稿のdeletedAtを設定する")
    void 投稿削除_delete_対象投稿のdeletedAtを設定する() {
        Post post = new Post("alice", "削除対象です", Instant.parse("2026-06-30T10:15:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        Instant before = Instant.now();

        postService.delete(1L);

        Instant after = Instant.now();
        assertThat(post.getDeletedAt()).isBetween(before, after);
        verify(postRepository).findById(1L);
    }
}
