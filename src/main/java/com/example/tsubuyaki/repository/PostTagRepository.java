package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {
}
