package com.example.tsubuyaki.service;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.domain.PostLike;
import com.example.tsubuyaki.repository.PostLikeRepository;
import com.example.tsubuyaki.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LikeService {

    // ServiceはLikeの追加/解除と件数取得の業務処理を担当する。
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    public LikeService(PostRepository postRepository, PostLikeRepository postLikeRepository) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
    }

    @Transactional
    public void toggle(Long postId, String clientHash) {
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        // 既にLike済みなら解除し、未Likeなら新規登録する。
        postLikeRepository.findByPostIdAndClientHash(postId, clientHash)
                .ifPresentOrElse(postLikeRepository::delete,
                        () -> postLikeRepository.save(new PostLike(post, clientHash)));
    }

    public long countByPostId(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }
}
