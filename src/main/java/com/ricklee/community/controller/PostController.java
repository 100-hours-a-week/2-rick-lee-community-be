package com.ricklee.community.controller;

import com.ricklee.community.dto.common.ApiResponse;
import com.ricklee.community.dto.post.PostDetailResponseDto;
import com.ricklee.community.dto.post.PostListItemDto;
import com.ricklee.community.dto.post.PostRequestDto;
import com.ricklee.community.service.PostService;
import com.ricklee.community.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 게시글 관련 API를 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final UserService userService;

    /**
     * 생성자 주입을 통한 의존성 주입
     */
    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    /**
     * 게시글 작성 API
     * POST /posts
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> createPost(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody PostRequestDto requestDto) {
        Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
        Long postId = postService.createPost(userId, requestDto);

        Map<String, Long> data = new HashMap<>();
        data.put("post_id", postId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("post_created", data));
    }

    /**
     * 특정 게시글 조회 API
     * GET /posts/{postId}
     */
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponseDto>> getPost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId) {
        Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
        PostDetailResponseDto postDetail = postService.getPostDetail(postId, userId);

        return ResponseEntity
                .ok(ApiResponse.success("post_fetched", postDetail));
    }

    /**
     * 게시글 수정 API
     * PUT /posts/{postId}
     */
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updatePost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId,
            @Valid @RequestBody PostRequestDto requestDto) {
        Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
        Map<String, Object> updatedPost = postService.updatePost(userId, postId, requestDto);

        return ResponseEntity
                .ok(ApiResponse.success("post_updated", updatedPost));
    }

    /**
     * 게시글 삭제 API
     * DELETE /posts/{postId}
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId) {
        Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
        postService.deletePost(userId, postId);

        return ResponseEntity
                .ok(ApiResponse.success("post_deleted"));
    }

    /**
     * 게시글 목록 조회 API
     * GET /posts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPostList(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int per_page) {
        // 토큰 유효성 검사
        userService.getUserIdFromToken(token.replace("Bearer ", ""));

        // 페이지 인덱스는 0부터 시작하므로 1을 빼줌
        List<PostListItemDto> posts = postService.getPostList(page - 1, per_page);
        Map<String, Object> pagination = postService.getPaginationInfo(page, per_page);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("posts", posts);
        responseData.put("pagination", pagination);

        return ResponseEntity
                .ok(ApiResponse.success("posts_list_retrieved", responseData));
    }

    /**
     * 게시글 좋아요 추가 API
     * POST /posts/{postId}/like
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<Map<String, Long>>> addLike(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId) {
        Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
        Long likeId = postService.addLike(userId, postId);

        Map<String, Long> data = new HashMap<>();
        data.put("id", likeId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("like_created", data));
    }

    /**
     * 게시글 좋아요 취소 API
     * DELETE /posts/{postId}/like
     */
    @DeleteMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<Map<String, Long>>> removeLike(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId) {
        Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
        Long removedLikeId = postService.removeLike(userId, postId);

        Map<String, Long> data = new HashMap<>();
        data.put("id", removedLikeId);

        return ResponseEntity
                .ok(ApiResponse.success("like_removed", data));
    }
}