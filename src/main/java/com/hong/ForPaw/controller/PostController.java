package com.hong.ForPaw.controller;

import com.hong.ForPaw.controller.DTO.PostRequest;
import com.hong.ForPaw.controller.DTO.PostResponse;
import com.hong.ForPaw.core.security.CustomUserDetails;
import com.hong.ForPaw.core.utils.ApiUtils;
import com.hong.ForPaw.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody @Valid PostRequest.CreatePostDTO requestDTO, @AuthenticationPrincipal CustomUserDetails userDetails){
        PostResponse.CreatePostDTO responseDTO = postService.createPost(requestDTO, userDetails.getUser().getId());
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, responseDTO));
    }

    @PostMapping("/posts/{postId}/qna")
    public ResponseEntity<?> createAnswer(@RequestBody @Valid PostRequest.CreateAnswerDTO requestDTO, @PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails userDetails){
        PostResponse.CreateAnswerDTO responseDTO = postService.createAnswer(requestDTO, postId, userDetails.getUser().getId());
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, responseDTO));
    }

    @GetMapping("/posts")
    public ResponseEntity<?> findPostList(){
        PostResponse.FindAllPostDTO responseDTO = postService.findPostList();
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, responseDTO));
    }

    @GetMapping("/posts/adoption")
    public ResponseEntity<?> findAdoptionPostList(@RequestParam("page") Integer page, @RequestParam("sort") String sort){
        PostResponse.FindAdoptionPostListDTO responseDTO = postService.findAdoptionPostList(page, sort);
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, responseDTO));
    }

    @GetMapping("/posts/protection")
    public ResponseEntity<?> findProtectionPostList(@RequestParam("page") Integer page, @RequestParam("sort") String sort){
        PostResponse.FindProtectionPostListDTO responseDTO = postService.findProtectionPostList(page, sort);
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, responseDTO));
    }

    @GetMapping("/posts/question")
    public ResponseEntity<?> findQuestionPostList(@RequestParam("page") Integer page, @RequestParam("sort") String sort){
        PostResponse.FindQnaPostListDTO responseDTO = postService.findQuestionPostList(page, sort);
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, responseDTO));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<?> findPostById(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails userDetails){
        PostResponse.FindPostByIdDTO responseDTO = postService.findPostById(postId, userDetails.getUser().getId());
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, responseDTO));
    }

    @GetMapping("/posts/{postId}/qna")
    public ResponseEntity<?> findQnaById(@PathVariable Long postId){
        PostResponse.FIndQnaByIdDTO responseDTO = postService.findQnaById(postId);
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, responseDTO));
    }

    @PatchMapping("/posts/{postId}")
    public ResponseEntity<?> updatePost(@RequestBody @Valid PostRequest.UpdatePostDTO requestDTO, @PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails userDetails){
        postService.updatePost(requestDTO, userDetails.getUser(), postId);
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, null));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails userDetails){
        postService.deletePost(postId, userDetails.getUser());
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, null));
    }

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails userDetails){
        postService.likePost(postId, userDetails.getUser().getId());
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, null));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> createComment(@RequestBody @Valid PostRequest.CreateCommentDTO requestDTO, @PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails userDetails){
        PostResponse.CreateCommentDTO responseDTO = postService.createComment(requestDTO, userDetails.getUser().getId(), postId);
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, responseDTO));
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/reply")
    public ResponseEntity<?> createReply(@RequestBody @Valid PostRequest.CreateCommentDTO requestDTO, @PathVariable Long postId, @PathVariable Long commentId, @AuthenticationPrincipal CustomUserDetails userDetails){
        PostResponse.CreateCommentDTO responseDTO = postService.createReply(requestDTO, postId, userDetails.getUser().getId(), commentId);
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, responseDTO));
    }

    @PatchMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<?> updateComment(@RequestBody @Valid PostRequest.UpdateCommentDTO requestDTO, @PathVariable Long postId, @PathVariable Long commentId, @AuthenticationPrincipal CustomUserDetails userDetails){
        postService.updateComment(requestDTO, postId, commentId, userDetails.getUser());
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, null));
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long postId, @PathVariable Long commentId, @AuthenticationPrincipal CustomUserDetails userDetails){
        postService.deleteComment(postId, commentId, userDetails.getUser());
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, null));
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/like")
    public ResponseEntity<?> likeComment(@PathVariable Long commentId, @AuthenticationPrincipal CustomUserDetails userDetails){
        postService.likeComment(commentId, userDetails.getUser().getId());
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, null));
    }

    @PostMapping("/reports")
    public ResponseEntity<?> submitReport(@RequestBody @Valid PostRequest.SubmitReport requestDTO, @AuthenticationPrincipal CustomUserDetails userDetails){
        postService.submitReport(requestDTO, userDetails.getUser().getId());
        return ResponseEntity.ok().body(ApiUtils.success(HttpStatus.OK, null));
    }
}