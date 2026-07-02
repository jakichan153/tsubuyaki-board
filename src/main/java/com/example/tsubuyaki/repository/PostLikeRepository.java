package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // RepositoryはLikeの重複判定と件数集計に必要なDB問い合わせを担当する。
    Optional<PostLike> findByPostIdAndClientHash(Long postId, String clientHash);

    long countByPostId(Long postId);
}
