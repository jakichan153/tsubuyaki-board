package com.example.tsubuyaki.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentForm {

    @NotBlank(message = "投稿者名を入力してください")
    @Size(max = 30, message = "投稿者名は 30 文字以内で入力してください")
    private String author;

    @NotBlank(message = "コメントを入力してください")
    @Size(max = 280, message = "コメントは 280 文字以内で入力してください")
    private String body;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
