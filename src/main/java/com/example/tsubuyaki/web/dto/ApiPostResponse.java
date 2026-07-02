package com.example.tsubuyaki.web.dto;

import com.example.tsubuyaki.domain.Post;

import java.time.Instant;

public record ApiPostResponse(
        Long id,
        String author,
        String body,
        String avatarColor,
        Instant createdAt) {

    // REST APIでは画面用Entityを必要なJSON項目だけのDTOへ変換する。
    public static ApiPostResponse from(Post post) {
        return new ApiPostResponse(
                post.getId(),
                post.getAuthor(),
                post.getBody(),
                post.getAvatarColor(),
                post.getCreatedAt());
    }
}
