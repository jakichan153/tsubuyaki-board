package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostTag;
import com.example.tsubuyaki.domain.Tag;
import com.example.tsubuyaki.repository.PostTagRepository;
import com.example.tsubuyaki.repository.PostRepository;
import com.example.tsubuyaki.repository.TagRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private PostTagRepository postTagRepository;

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
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

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
    @DisplayName("投稿作成_本文中のタグを抽出して保存する")
    void 投稿作成_本文中のタグを抽出して保存する() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(tagRepository.findByName("java")).willReturn(Optional.empty());
        given(tagRepository.save(any(Tag.class))).willAnswer(invocation -> invocation.getArgument(0));

        postService.create("alice", "今日は #java を学びました", "blue");

        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).save(tagCaptor.capture());
        assertThat(tagCaptor.getValue().getName()).isEqualTo("java");
        ArgumentCaptor<PostTag> postTagCaptor = ArgumentCaptor.forClass(PostTag.class);
        verify(postTagRepository).save(postTagCaptor.capture());
        assertThat(postTagCaptor.getValue().getTag().getName()).isEqualTo("java");
    }

    @Test
    @DisplayName("投稿作成_複数タグをすべて保存する")
    void 投稿作成_複数タグをすべて保存する() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(tagRepository.findByName("java")).willReturn(Optional.empty());
        given(tagRepository.findByName("spring")).willReturn(Optional.empty());
        given(tagRepository.save(any(Tag.class))).willAnswer(invocation -> invocation.getArgument(0));

        postService.create("alice", "#java と #spring を使います", "blue");

        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository, times(2)).save(tagCaptor.capture());
        assertThat(tagCaptor.getAllValues()).extracting(Tag::getName)
                .containsExactly("java", "spring");
        verify(postTagRepository, times(2)).save(any(PostTag.class));
    }

    @Test
    @DisplayName("投稿作成_タグを含まない投稿でも正常に登録できる")
    void 投稿作成_タグを含まない投稿でも正常に登録できる() {
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

        postService.create("alice", "タグなし本文です", "blue");

        verify(tagRepository, never()).save(any(Tag.class));
        verify(postTagRepository, never()).save(any(PostTag.class));
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
