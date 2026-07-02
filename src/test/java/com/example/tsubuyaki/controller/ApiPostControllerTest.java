package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ ApiPostController.class, OpenApiController.class })
class ApiPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("REST_API_GET_api_posts_HTTP200を返す")
    void REST_API_GET_api_posts_HTTP200を返す() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("REST_API_GET_api_posts_JSON形式で投稿一覧を返す")
    void REST_API_GET_api_posts_JSON形式で投稿一覧を返す() throws Exception {
        Post post = new Post("alice", "APIで返す本文です", "blue", Instant.parse("2026-06-30T10:15:00Z"));
        setPostId(post, 1L);
        given(postService.latest()).willReturn(List.of(post));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].author").value("alice"))
                .andExpect(jsonPath("$[0].body").value("APIで返す本文です"))
                .andExpect(jsonPath("$[0].avatarColor").value("blue"))
                .andExpect(jsonPath("$[0].createdAt").value("2026-06-30T10:15:00Z"));
    }

    @Test
    @DisplayName("REST_API_GET_api_posts_投稿が存在しない場合は空配列を返す")
    void REST_API_GET_api_posts_投稿が存在しない場合は空配列を返す() throws Exception {
        given(postService.latest()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("OpenAPIドキュメント_openapi_yamlを表示できる")
    void OpenAPIドキュメント_openapi_yamlを表示できる() throws Exception {
        mockMvc.perform(get("/openapi.yaml"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("openapi: 3.0.3")))
                .andExpect(content().string(containsString("/api/posts")));
    }

    private static void setPostId(Post post, Long id) throws NoSuchFieldException, IllegalAccessException {
        Field idField = Post.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(post, id);
    }
}
