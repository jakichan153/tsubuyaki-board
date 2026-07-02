package com.example.tsubuyaki.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

@Entity
@Table(name = "posts")
public class Post {

    public static final String DEFAULT_AVATAR_COLOR = "gray";

    @Id
    @SequenceGenerator(name = "posts_seq_gen", sequenceName = "posts_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "posts_seq_gen")
    private Long id;

    @Column(name = "author", length = 30, nullable = false)
    private String author;

    @Column(name = "body", length = 280, nullable = false)
    private String body;

    @Column(name = "avatar_color", length = 20, nullable = false)
    private String avatarColor;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "image_content_type", length = 100)
    private String imageContentType;

    @Lob
    @Column(name = "image_data")
    private byte[] imageData;

    protected Post() {
        // JPA
    }

    public Post(String author, String body, Instant createdAt) {
        this(author, body, DEFAULT_AVATAR_COLOR, createdAt);
    }

    public Post(String author, String body, String avatarColor, Instant createdAt) {
        this.author = author;
        this.body = body;
        this.avatarColor = avatarColor;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getBody() {
        return body;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void markDeleted(Instant deletedAt) {
        // 論理削除は削除日時だけを記録し、投稿データ自体は残す。
        this.deletedAt = deletedAt;
    }

    public String getImageContentType() {
        return imageContentType;
    }

    public byte[] getImageData() {
        return imageData == null ? null : imageData.clone();
    }

    public boolean hasImage() {
        return imageContentType != null && imageData != null && imageData.length > 0;
    }

    public String getImageDataUri() {
        if (!hasImage()) {
            return "";
        }
        // Thymeleafから直接表示できるdata URIへ変換する。
        return "data:" + imageContentType + ";base64,"
                + Base64.getEncoder().encodeToString(imageData);
    }

    public void attachImage(String imageContentType, byte[] imageData) {
        // 添付画像は投稿1件につき1枚だけ保持する。
        this.imageContentType = imageContentType;
        this.imageData = imageData == null ? null : imageData.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Post other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
