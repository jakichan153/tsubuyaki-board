package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.domain.Post;
import com.example.tsubuyaki.service.CommentService;
import com.example.tsubuyaki.service.LikeService;
import com.example.tsubuyaki.service.PostNotFoundException;
import com.example.tsubuyaki.service.PostService;
import com.example.tsubuyaki.web.dto.CommentForm;
import com.example.tsubuyaki.web.dto.PostForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class PostController {

    // Controllerは画面入力を受け取り、表示に必要な値をServiceから取得する。
    private final PostService postService;
    private final LikeService likeService;
    private final CommentService commentService;

    public PostController(PostService postService, LikeService likeService, CommentService commentService) {
        this.postService = postService;
        this.likeService = likeService;
        this.commentService = commentService;
    }

    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(@RequestParam(name = "q", required = false) String q, Model model) {
        // キーワードがある場合だけ検索し、通常の一覧表示は最新投稿を取得する。
        List<Post> posts = hasSearchKeyword(q) ? postService.searchByBody(q) : postService.latest();
        model.addAttribute("posts", posts);
        model.addAttribute("likeCounts", likeCounts(posts));
        model.addAttribute("commentCounts", commentCounts(posts));
        model.addAttribute("q", q == null ? "" : q);
        return "posts/list";
    }

    private static boolean hasSearchKeyword(String q) {
        return q != null && !q.isBlank();
    }

    @GetMapping("/tags/{name}")
    public String listByTag(@PathVariable String name, Model model) {
        // タグ名に紐づく投稿一覧をServiceへ問い合わせる。
        List<Post> posts = postService.findByTagName(name);
        model.addAttribute("posts", posts);
        model.addAttribute("likeCounts", likeCounts(posts));
        model.addAttribute("commentCounts", commentCounts(posts));
        model.addAttribute("q", "");
        model.addAttribute("tagName", name);
        return "posts/list";
    }

    @GetMapping("/posts/new")
    public String newForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        return "posts/form";
    }

    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute("postForm") PostForm postForm,
            BindingResult bindingResult) {
        // 入力エラー時は投稿作成画面を再表示し、登録処理へ進めない。
        if (bindingResult.hasErrors()) {
            return "posts/form";
        }

        createPost(postForm);
        return "redirect:/posts";
    }

    private void createPost(PostForm postForm) {
        MultipartFile image = postForm.getImage();
        // 画像未添付の投稿は従来どおり本文だけで登録する。
        if (image == null || image.isEmpty()) {
            postService.create(postForm.getAuthor(), postForm.getBody(), postForm.getAvatarColor());
            return;
        }
        try {
            // 添付画像は1枚だけServiceへ渡し、保存方式はService/Entity側に任せる。
            postService.create(postForm.getAuthor(), postForm.getBody(), postForm.getAvatarColor(),
                    image.getContentType(), image.getBytes());
        } catch (IOException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Image upload failed", e);
        }
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model, HttpServletRequest request) {
        addPostDetailModel(id, model, request);
        return "posts/detail";
    }

    @PostMapping("/posts/{id}/comments")
    public String createComment(@PathVariable Long id,
            @Valid @ModelAttribute("commentForm") CommentForm commentForm,
            BindingResult bindingResult, Model model, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            addPostDetailModel(id, model, request);
            return "posts/detail";
        }
        try {
            commentService.create(id, commentForm.getAuthor(), commentForm.getBody());
        } catch (PostNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, "Post not found", e);
        }
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request) {
        try {
            // 同じ利用者のLikeはService側で追加/解除を切り替える。
            likeService.toggle(id, clientHash(request));
        } catch (PostNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, "Post not found", e);
        }
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id) {
        try {
            // 削除操作はServiceへ委譲し、永続化層では論理削除として扱う。
            postService.delete(id);
        } catch (PostNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, "Post not found", e);
        }
        return "redirect:/posts";
    }

    private static String clientHash(HttpServletRequest request) {
        String source = request.getRemoteAddr() + request.getHeader("User-Agent");
        try {
            // IPアドレスとUser-Agentを短いハッシュにして、簡易的なLike識別子にする。
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private Map<Long, Long> likeCounts(List<Post> posts) {
        Map<Long, Long> counts = new LinkedHashMap<>();
        for (Post post : posts) {
            counts.put(post.getId(), likeService.countByPostId(post.getId()));
        }
        return counts;
    }

    private Map<Long, Long> commentCounts(List<Post> posts) {
        Map<Long, Long> counts = new LinkedHashMap<>();
        for (Post post : posts) {
            if (post.getId() != null) {
                counts.put(post.getId(), commentService.countByPostId(post.getId()));
            }
        }
        return counts;
    }

    private void addPostDetailModel(Long id, Model model, HttpServletRequest request) {
        model.addAttribute("post", postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND)));
        model.addAttribute("likeCount", likeService.countByPostId(id));
        model.addAttribute("liked", likeService.isLiked(id, clientHash(request)));
        model.addAttribute("comments", commentService.latestByPostId(id));
        model.addAttribute("commentCount", commentService.countByPostId(id));
        if (!model.containsAttribute("commentForm")) {
            model.addAttribute("commentForm", new CommentForm());
        }
    }
}
