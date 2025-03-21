package com.ricklee.community.controller;

import com.ricklee.community.dto.ApiResponse;
import com.ricklee.community.dto.PostDetailResponseDto;
import com.ricklee.community.dto.PostListItemDto;
import com.ricklee.community.dto.PostRequestDto;
import com.ricklee.community.exception.ResourceNotFoundException;
import com.ricklee.community.exception.UnauthorizedException;
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
    public ResponseEntity<Map<String, Object>> createPost(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody PostRequestDto requestDto) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
            Long postId = postService.createPost(userId, requestDto);

            Map<String, Object> data = new HashMap<>();
            data.put("post_id", postId);

            response.put("message", "post_created");
            response.put("data", data);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (UnauthorizedException e) {
            response.put("message", "unauthorized");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (ResourceNotFoundException e) {
            response.put("message", e.getResourceType() + "_not_found");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("message", "invalid_request");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 특정 게시글 조회 API
     * GET /posts/{postId}
     */
    @GetMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> getPost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
            PostDetailResponseDto postDetail = postService.getPostDetail(postId, userId);

            response.put("message", "post_fetched");
            response.put("data", postDetail);

            return ResponseEntity.ok(response);
        } catch (UnauthorizedException e) {
            response.put("message", "unauthorized");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (ResourceNotFoundException e) {
            response.put("message", "post_not_found");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("message", "invalid_request");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 게시글 수정 API
     * PUT /posts/{postId}
     */
    @PutMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> updatePost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId,
            @Valid @RequestBody PostRequestDto requestDto) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
            Map<String, Object> updatedPost = postService.updatePost(userId, postId, requestDto);

            response.put("message", "post_updated");
            response.put("data", updatedPost);

            return ResponseEntity.ok(response);
        } catch (UnauthorizedException e) {
            response.put("message", "unauthorized");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (ResourceNotFoundException e) {
            response.put("message", "post_not_found");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("message", "invalid_request");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 게시글 삭제 API
     * DELETE /posts/{postId}
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> deletePost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
            postService.deletePost(userId, postId);

            response.put("message", "post_deleted");
            response.put("data", null);

            return ResponseEntity.ok(response);
        } catch (UnauthorizedException e) {
            response.put("message", "unauthorized");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (ResourceNotFoundException e) {
            response.put("message", "post_not_found");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("message", "invalid_request");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 게시글 목록 조회 API
     * GET /posts
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPostList(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int per_page) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 토큰 유효성 검사
            userService.getUserIdFromToken(token.replace("Bearer ", ""));

            // 페이지 인덱스는 0부터 시작하므로 1을 빼줌
            List<PostListItemDto> posts = postService.getPostList(page - 1, per_page);

            // 페이지네이션 정보 가져오기
            Map<String, Object> pagination = postService.getPaginationInfo(page, per_page);

            response.put("message", "posts_list_retrieved");
            response.put("data", posts);
            response.put("pagination", pagination);

            return ResponseEntity.ok(response);
        } catch (UnauthorizedException e) {
            response.put("message", "unauthorized");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("message", "invalid_request");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 게시글 좋아요 추가 API
     * POST /posts/{postId}/like
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<Map<String, Object>> addLike(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
            Long likeId = postService.addLike(userId, postId);

            Map<String, Object> data = new HashMap<>();
            data.put("id", likeId);

            response.put("message", "like_created");
            response.put("data", data);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (UnauthorizedException e) {
            response.put("message", "unauthorized");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (ResourceNotFoundException e) {
            response.put("message", "post_not_found");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalArgumentException e) {
            // 중복 좋아요 등 유효성 검사 실패
            response.put("message", "invalid_request");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("message", "invalid_request");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 게시글 좋아요 취소 API
     * DELETE /posts/{postId}/like
     */
    @DeleteMapping("/{postId}/like")
    public ResponseEntity<Map<String, Object>> removeLike(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
            Long removedLikeId = postService.removeLike(userId, postId);

            Map<String, Object> data = new HashMap<>();
            data.put("id", removedLikeId);

            response.put("message", "like_removed");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (UnauthorizedException e) {
            response.put("message", "unauthorized");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (ResourceNotFoundException e) {
            response.put("message", "post_not_found");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("like_not_found")) {
                response.put("message", "post_not_found");
                response.put("error", e.getMessage());
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else {
                response.put("message", "invalid_request");
                response.put("error", e.getMessage());
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("message", "invalid_request");
            response.put("error", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}