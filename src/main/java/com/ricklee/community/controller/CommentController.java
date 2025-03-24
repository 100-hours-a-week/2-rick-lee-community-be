package com.ricklee.community.controller;

import com.ricklee.community.dto.common.ApiResponse;
import com.ricklee.community.dto.comment.CommentRequestDto;
import com.ricklee.community.service.CommentService;
import com.ricklee.community.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 댓글 관련 API를 처리하는 컨트롤러
 */
@RestController
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    public CommentController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    /**
     * 댓글 작성 API
     * POST /comments
     */
    @PostMapping("/comments")
    public ResponseEntity<ApiResponse<Map<String, Long>>> createComment(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CommentRequestDto requestDto) {
        Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
        Long commentId = commentService.createComment(userId, requestDto);

        Map<String, Long> data = new HashMap<>();
        data.put("comment_id", commentId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("comment_created", data));
    }

    /**
     * 댓글 목록 조회 API
     * GET /posts/{postId}/comments
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getComments(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId) {
        // 토큰 유효성 검사
        userService.getUserIdFromToken(token.replace("Bearer ", ""));
        List<Map<String, Object>> comments = commentService.getCommentsByPostId(postId);

        return ResponseEntity
                .ok(ApiResponse.success("comments_retrieved", comments));
    }

    /**
     * 댓글 수정 API
     * PUT /comments/{commentId}
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateComment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long commentId,
            @Valid @RequestBody Map<String, String> requestBody) {
        Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
        String content = requestBody.get("content");

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 필수 입력 항목입니다.");
        }

        Map<String, Object> updatedComment = commentService.updateComment(userId, commentId, content);

        return ResponseEntity
                .ok(ApiResponse.success("comment_updated", updatedComment));
    }

    /**
     * 댓글 삭제 API
     * DELETE /comments/{commentId}
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Map<String, Long>>> deleteComment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long commentId) {
        Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
        commentService.deleteComment(userId, commentId);

        Map<String, Long> data = new HashMap<>();
        data.put("comment_id", commentId);

        return ResponseEntity
                .ok(ApiResponse.success("comment_deleted", data));
    }
}