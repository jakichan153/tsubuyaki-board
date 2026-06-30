package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.PostForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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
}
