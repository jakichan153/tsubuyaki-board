package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // Repositoryは投稿の永続化と、一覧・検索用のDB問い合わせを担当する。
    List<Post> findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

    List<Post> findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc(String keyword);

    // タグ名から投稿を結合検索し、論理削除済みの投稿は除外する。
    @Query("""
            SELECT pt.post
            FROM PostTag pt
            WHERE pt.tag.name = :name
              AND pt.post.deletedAt IS NULL
            ORDER BY pt.post.createdAt DESC
            """)
    List<Post> findByTagNameOrderByCreatedAtDesc(@Param("name") String name);
}
