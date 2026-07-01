package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @InjectMocks
    private LikeService likeService;

    @Test
    @DisplayName("いいね_未いいねの場合_いいねを追加する")
    void いいね_未いいねの場合_いいねを追加する() {
        Post post = new Post("alice", "本文です", Instant.parse("2026-06-30T10:15:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "client001")).willReturn(Optional.empty());

        likeService.toggle(1L, "client001");

        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);
        verify(postLikeRepository).save(captor.capture());
        PostLike saved = captor.getValue();
        assertThat(saved.getPost()).isSameAs(post);
        assertThat(saved.getClientHash()).isEqualTo("client001");
        verify(postLikeRepository, never()).delete(org.mockito.ArgumentMatchers.any(PostLike.class));
    }

    @Test
    @DisplayName("いいね_同一clientHashが再実行した場合_いいねを解除する")
    void いいね_同一clientHashが再実行した場合_いいねを解除する() {
        Post post = new Post("alice", "本文です", Instant.parse("2026-06-30T10:15:00Z"));
        PostLike existing = new PostLike(post, "client001");
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postLikeRepository.findByPostIdAndClientHash(1L, "client001")).willReturn(Optional.of(existing));

        likeService.toggle(1L, "client001");

        verify(postLikeRepository).delete(existing);
        verify(postLikeRepository, never()).save(org.mockito.ArgumentMatchers.any(PostLike.class));
    }

    @Test
    @DisplayName("いいね数_countByPostId_Repositoryの件数を返す")
    void いいね数_countByPostId_Repositoryの件数を返す() {
        given(postLikeRepository.countByPostId(1L)).willReturn(2L);

        long actual = likeService.countByPostId(1L);

        assertThat(actual).isEqualTo(2L);
        verify(postLikeRepository).countByPostId(1L);
    }

    @Test
    @DisplayName("いいね_存在しない投稿id_PostNotFoundExceptionを投げる")
    void いいね_存在しない投稿id_PostNotFoundExceptionを投げる() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.toggle(999L, "client001"))
                .isInstanceOf(PostNotFoundException.class);

        verify(postLikeRepository, never()).save(org.mockito.ArgumentMatchers.any(PostLike.class));
        verify(postLikeRepository, never()).delete(org.mockito.ArgumentMatchers.any(PostLike.class));
    }
}
