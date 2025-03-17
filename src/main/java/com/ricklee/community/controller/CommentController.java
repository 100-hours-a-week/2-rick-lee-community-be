package com.ricklee.community.controller;

import com.ricklee.community.dto.CommentRequestDto;
import com.ricklee.community.service.CommentService;
import com.ricklee.community.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> createComment(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CommentRequestDto requestDto) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
            Long commentId = commentService.createComment(userId, requestDto);

            Map<String, Object> data = new HashMap<>();
            data.put("comment_id", commentId);

            response.put("message", "comment_created");
            response.put("data", data);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("post_not_found")) {
                response.put("message", "post_not_found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else {
                response.put("message", "unauthorized");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            response.put("message", "internal_server_error");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 댓글 목록 조회 API
     * GET /posts/{postId}/comments
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Map<String, Object>> getComments(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 토큰 유효성 검사 (실제 데이터는 사용하지 않으므로 변수에 할당하지 않음)
            userService.getUserIdFromToken(token.replace("Bearer ", ""));

            List<Map<String, Object>> comments = commentService.getCommentsByPostId(postId);

            response.put("message", "comments_retrieved");
            response.put("data", comments);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("post_not_found")) {
                response.put("message", "post_not_found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else {
                response.put("message", "unauthorized");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            response.put("message", "internal_server_error");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 댓글 수정 API
     * PUT /comments/{commentId}
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<Map<String, Object>> updateComment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long commentId,
            @Valid @RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
            String content = requestBody.get("content");

            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("invalid_request");
            }

            Map<String, Object> updatedComment = commentService.updateComment(userId, commentId, content);

            response.put("message", "comment_updated");
            response.put("data", updatedComment);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("comment_not_found")) {
                response.put("message", "post_not_found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else if (e.getMessage().equals("unauthorized")) {
                response.put("message", "unauthorized");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            } else {
                response.put("message", "invalid_request");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("message", "internal_server_error");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 댓글 삭제 API
     * DELETE /comments/{commentId}
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long commentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
            commentService.deleteComment(userId, commentId);

            Map<String, Object> data = new HashMap<>();
            data.put("comment_id", commentId);

            response.put("message", "comment_deleted");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("comment_not_found")) {
                response.put("message", "post_not_found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else if (e.getMessage().equals("unauthorized")) {
                response.put("message", "unauthorized");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            } else {
                response.put("message", "invalid_request");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("message", "internal_server_error");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}