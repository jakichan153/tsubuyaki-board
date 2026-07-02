package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findTop50ByDeletedAtIsNullOrderByCreatedAtDesc();

    List<Post> findTop50ByBodyContainingAndDeletedAtIsNullOrderByCreatedAtDesc(String keyword);

    @Query("""
            SELECT pt.post
            FROM PostTag pt
            WHERE pt.tag.name = :name
              AND pt.post.deletedAt IS NULL
            ORDER BY pt.post.createdAt DESC
            """)
    List<Post> findByTagNameOrderByCreatedAtDesc(@Param("name") String name);
}
