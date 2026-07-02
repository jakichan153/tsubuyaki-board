package com.example.tsubuyaki.controller;

import com.example.tsubuyaki.service.LikeService;
import com.example.tsubuyaki.service.PostNotFoundException;
import com.example.tsubuyaki.service.PostService;
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class PostController {

    private final PostService postService;
    private final LikeService likeService;

    public PostController(PostService postService, LikeService likeService) {
        this.postService = postService;
        this.likeService = likeService;
    }

    @GetMapping({ "/", "/posts", "/posts/" })
    public String list(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("posts", hasSearchKeyword(q) ? postService.searchByBody(q) : postService.latest());
        model.addAttribute("q", q == null ? "" : q);
        return "posts/list";
    }

    private static boolean hasSearchKeyword(String q) {
        return q != null && !q.isBlank();
    }

    @GetMapping("/tags/{name}")
    public String listByTag(@PathVariable String name, Model model) {
        model.addAttribute("posts", postService.findByTagName(name));
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
        if (bindingResult.hasErrors()) {
            return "posts/form";
        }

        postService.create(postForm.getAuthor(), postForm.getBody(), postForm.getAvatarColor());
        return "redirect:/posts";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("post", postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND)));
        model.addAttribute("likeCount", likeService.countByPostId(id));
        return "posts/detail";
    }

    @PostMapping("/posts/{id}/likes")
    public String toggleLike(@PathVariable Long id, HttpServletRequest request) {
        try {
            likeService.toggle(id, clientHash(request));
        } catch (PostNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, "Post not found", e);
        }
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id) {
        try {
            postService.delete(id);
        } catch (PostNotFoundException e) {
            throw new ResponseStatusException(NOT_FOUND, "Post not found", e);
        }
        return "redirect:/posts";
    }

    private static String clientHash(HttpServletRequest request) {
        String source = request.getRemoteAddr() + request.getHeader("User-Agent");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
