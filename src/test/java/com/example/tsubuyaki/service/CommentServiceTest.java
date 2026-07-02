package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostComment;
import com.example.tsubuyaki.repository.PostCommentRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostCommentRepository postCommentRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("コメント投稿_create_投稿に紐づけて保存する")
    void コメント投稿_create_投稿に紐づけて保存する() {
        Post post = new Post("alice", "本文です", Instant.parse("2026-06-30T10:00:00Z"));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postCommentRepository.save(any(PostComment.class))).willAnswer(invocation -> invocation.getArgument(0));

        commentService.create(1L, "bob", "コメントです");

        ArgumentCaptor<PostComment> captor = ArgumentCaptor.forClass(PostComment.class);
        verify(postCommentRepository).save(captor.capture());
        PostComment saved = captor.getValue();
        assertThat(saved.getPost()).isSameAs(post);
        assertThat(saved.getAuthor()).isEqualTo("bob");
        assertThat(saved.getBody()).isEqualTo("コメントです");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("コメント投稿_create_存在しない投稿id_PostNotFoundExceptionを投げる")
    void コメント投稿_create_存在しない投稿id_PostNotFoundExceptionを投げる() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(999L, "bob", "コメントです"))
                .isInstanceOf(PostNotFoundException.class);
    }

    @Test
    @DisplayName("コメント一覧_latestByPostId_Repositoryの新着順コメントを返す")
    void コメント一覧_latestByPostId_Repositoryの新着順コメントを返す() {
        Post post = new Post("alice", "本文です", Instant.parse("2026-06-30T10:00:00Z"));
        List<PostComment> comments = List.of(
                new PostComment(post, "bob", "新しいコメント", Instant.parse("2026-06-30T12:00:00Z")),
                new PostComment(post, "carol", "古いコメント", Instant.parse("2026-06-30T11:00:00Z"))
        );
        given(postCommentRepository.findByPostIdOrderByCreatedAtDesc(1L)).willReturn(comments);

        List<PostComment> actual = commentService.latestByPostId(1L);

        assertThat(actual).isSameAs(comments);
        verify(postCommentRepository).findByPostIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("コメント数_countByPostId_Repositoryの件数を返す")
    void コメント数_countByPostId_Repositoryの件数を返す() {
        given(postCommentRepository.countByPostId(1L)).willReturn(3L);

        long actual = commentService.countByPostId(1L);

        assertThat(actual).isEqualTo(3L);
        verify(postCommentRepository).countByPostId(1L);
    }
}
