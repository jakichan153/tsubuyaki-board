package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.ApiPostResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class ApiPostController {

    // REST Controllerは画面ではなくJSONとして投稿一覧を返す。
    private final PostService postService;

    public ApiPostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public List<ApiPostResponse> list() {
        // APIでもServiceの一覧取得を使い、表示用DTOへ変換する。
        return postService.latest().stream()
                .map(ApiPostResponse::from)
                .toList();
    }
}
