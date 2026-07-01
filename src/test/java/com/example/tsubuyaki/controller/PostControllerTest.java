package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
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

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
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
}
