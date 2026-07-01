package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.LikeService;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private LikeService likeService;

    @Test
    @DisplayName("投稿一覧_0件の場合_まだ投稿はありませんを表示する")
    void 投稿一覧_0件の場合_まだ投稿はありませんを表示する() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", Collections.emptyList()))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿一覧_更新ボタン_GET_postsへリクエストできる")
    void 投稿一覧_更新ボタン_GET_postsへリクエストできる() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern(
                        "(?s).*<form[^>]*action=\"/posts/\"[^>]*method=\"get\"[^>]*>.*"
                                + "<button[^>]*type=\"submit\"[^>]*>更新</button>.*"
                )));
    }

    @Test
    @DisplayName("投稿一覧_投稿は投稿者内容投稿日の順に表示する")
    void 投稿一覧_投稿は投稿者内容投稿日の順に表示する() throws Exception {
        given(postService.latest()).willReturn(List.of(
                new Post("alice", "本文がここに表示されます", Instant.parse("2026-06-30T10:15:00Z"))
        ));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern(
                        "(?s).*alice.*本文がここに表示されます.*2026-06-30.*"
                )));
    }

    @Test
    @DisplayName("投稿一覧_各投稿に詳細リンクを表示する")
    void 投稿一覧_各投稿に詳細リンクを表示する() throws Exception {
        Post post = new Post("alice", "本文がここに表示されます", Instant.parse("2026-06-30T10:15:00Z"));
        setPostId(post, 1L);
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern(
                        "(?s).*<a[^>]*href=\"/posts/1\"[^>]*>詳細</a>.*"
                )));
    }

    @Test
    @DisplayName("投稿検索_q指定_一致する投稿のみ表示する")
    void 投稿検索_q指定_一致する投稿のみ表示する() throws Exception {
        Post post = new Post("alice", "検索できます", Instant.parse("2026-06-30T10:15:00Z"));
        given(postService.searchByBody("検索")).willReturn(List.of(post));

        mockMvc.perform(get("/posts").param("q", "検索"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/list"))
                .andExpect(model().attribute("posts", List.of(post)))
                .andExpect(model().attribute("q", "検索"))
                .andExpect(content().string(containsString("検索できます")));
    }

    @Test
    @DisplayName("投稿検索_q指定_一致しない場合は0件表示する")
    void 投稿検索_q指定_一致しない場合は0件表示する() throws Exception {
        given(postService.searchByBody("該当なし")).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts").param("q", "該当なし"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", Collections.emptyList()))
                .andExpect(model().attribute("q", "該当なし"))
                .andExpect(content().string(containsString("まだ投稿はありません")));
    }

    @Test
    @DisplayName("投稿検索_q空文字_全件表示する")
    void 投稿検索_q空文字_全件表示する() throws Exception {
        List<Post> posts = List.of(
                new Post("alice", "全件表示されます", Instant.parse("2026-06-30T10:15:00Z"))
        );
        given(postService.latest()).willReturn(posts);

        mockMvc.perform(get("/posts").param("q", ""))
                .andExpect(status().isOk())
                .andExpect(model().attribute("posts", posts))
                .andExpect(model().attribute("q", ""))
                .andExpect(content().string(containsString("全件表示されます")));
    }

    @Test
    @DisplayName("投稿検索_一覧画面_検索ボックスを表示する")
    void 投稿検索_一覧画面_検索ボックスを表示する() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern(
                        "(?s).*<form[^>]*action=\"/posts\"[^>]*method=\"get\"[^>]*>.*"
                                + "<input[^>]*name=\"q\"[^>]*>.*"
                                + "<button[^>]*type=\"submit\"[^>]*>検索</button>.*"
                )));
    }

    @Test
    @DisplayName("投稿フォーム_GET_posts_new_空のフォームをビューに渡す")
    void 投稿フォーム_GET_posts_new_空のフォームをビューに渡す() throws Exception {
        mockMvc.perform(get("/posts/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeExists("postForm"))
                .andExpect(model().attribute("postForm", instanceOf(PostForm.class)));
    }

    @Test
    @DisplayName("投稿作成_正常入力_Serviceに登録を依頼し一覧へリダイレクトする")
    void 投稿作成_正常入力_Serviceに登録を依頼し一覧へリダイレクトする() throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", "alice")
                        .param("body", "初投稿です"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts"))
                .andExpect(header().string("Location", "/posts"));

        verify(postService).create("alice", "初投稿です");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidPostForms")
    @DisplayName("投稿作成_入力不正_フォームを再表示し入力値とエラーメッセージを保持する")
    void 投稿作成_入力不正_フォームを再表示し入力値とエラーメッセージを保持する(
            String caseName, String author, String body, String expectedMessage) throws Exception {
        mockMvc.perform(post("/posts")
                        .param("author", author)
                        .param("body", body))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/form"))
                .andExpect(model().attributeHasErrors("postForm"))
                .andExpect(content().string(containsString(expectedMessage)))
                .andExpect(content().string(matchesPattern(
                        "(?s).*<input[^>]*id=\"author\"[^>]*value=\""
                                + Pattern.quote(author) + "\"[^>]*>.*")))
                .andExpect(content().string(matchesPattern(
                        "(?s).*<textarea[^>]*id=\"body\"[^>]*>"
                                + Pattern.quote(body) + "</textarea>.*")));

        verify(postService, never()).create(anyString(), anyString());
    }

    @Test
    @DisplayName("投稿詳細_存在するid_posts_detailを表示し投稿をビューに渡す")
    void 投稿詳細_存在するid_posts_detailを表示し投稿をビューに渡す() throws Exception {
        Post post = new Post("alice", "詳細本文です", Instant.parse("2026-06-30T10:15:00Z"));
        setPostId(post, 1L);
        given(postService.findById(1L)).willReturn(Optional.of(post));
        given(likeService.countByPostId(1L)).willReturn(3L);

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attribute("post", post))
                .andExpect(model().attribute("likeCount", 3L))
                .andExpect(content().string(matchesPattern(
                        "(?s).*alice.*詳細本文です.*2026-06-30.*いいね数.*3.*"
                )))
                .andExpect(content().string(matchesPattern(
                        "(?s).*<form[^>]*action=\"/posts/1/likes\"[^>]*method=\"post\"[^>]*>.*"
                                + "<button[^>]*type=\"submit\"[^>]*>Like</button>.*"
                )));
    }

    @Test
    @DisplayName("投稿詳細_存在しないid_404を返す")
    void 投稿詳細_存在しないid_404を返す() throws Exception {
        given(postService.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("いいね_POST_posts_id_likes_clientHashでトグルし詳細へリダイレクトする")
    void いいね_POST_posts_id_likes_clientHashでトグルし詳細へリダイレクトする() throws Exception {
        Post post = new Post("alice", "詳細本文です", Instant.parse("2026-06-30T10:15:00Z"));
        given(postService.findById(1L)).willReturn(Optional.of(post));

        mockMvc.perform(post("/posts/1/likes")
                        .header("User-Agent", "JUnit")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.10");
                            return request;
                        }))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/posts/1"));

        verify(likeService).toggle(1L, "e97515d4");
    }

    @Test
    @DisplayName("いいね_存在しないid_404を返す")
    void いいね_存在しないid_404を返す() throws Exception {
        willThrow(new com.example.tsubuyaki.service.PostNotFoundException())
                .given(likeService).toggle(999L, "e97515d4");

        mockMvc.perform(post("/posts/999/likes")
                        .header("User-Agent", "JUnit")
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.10");
                            return request;
                        }))
                .andExpect(status().isNotFound());
    }

    static Stream<Arguments> invalidPostForms() {
        String validAuthor = "alice";
        String validBody = "本文です";
        return Stream.of(
                Arguments.of(
                        "author必須", "", validBody, "投稿者名を入力してください"),
                Arguments.of(
                        "author30文字超過", "あ".repeat(31), validBody, "投稿者名は 30 文字以内で入力してください"),
                Arguments.of(
                        "author空白のみ", "   ", validBody, "投稿者名を入力してください"),
                Arguments.of(
                        "body必須", validAuthor, "", "本文を入力してください"),
                Arguments.of(
                        "body280文字超過", validAuthor, "あ".repeat(281), "本文は 280 文字以内で入力してください"),
                Arguments.of(
                        "body空白のみ", validAuthor, "   ", "本文を入力してください")
        );
    }

    private static void setPostId(Post post, Long id) throws NoSuchFieldException, IllegalAccessException {
        Field idField = Post.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(post, id);
    }
}
