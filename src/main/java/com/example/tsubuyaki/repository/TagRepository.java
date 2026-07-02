package com.example.tsubuyaki.repository;

import com.example.tsubuyaki.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    // Repositoryはタグ名による再利用判定に必要なDB問い合わせを担当する。
    Optional<Tag> findByName(String name);
}
