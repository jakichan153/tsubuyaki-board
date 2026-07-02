package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;

// Repositoryは投稿とタグの紐づけを永続化する。
public interface PostTagRepository extends JpaRepository<PostTag, Long> {
}
