package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPostIdOrderByCreatedAtDesc(Long postId);

    long countByPostId(Long postId);
}
