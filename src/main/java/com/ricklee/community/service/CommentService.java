package com.ricklee.community.service;

import com.ricklee.community.domain.Comment;
import com.ricklee.community.domain.Post;
import com.ricklee.community.domain.User;
import com.ricklee.community.dto.comment.CommentRequestDto;
import com.ricklee.community.dto.comment.CommentResponseDto;
import com.ricklee.community.exception.custom.ResourceNotFoundException;
import com.ricklee.community.exception.custom.UnauthorizedException;
import com.ricklee.community.repository.CommentRepository;
import com.ricklee.community.repository.PostRepository;
import com.ricklee.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 댓글 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 댓글 작성
     *
     * @param userId     작성자 ID
     * @param requestDto 댓글 작성 요청 정보
     * @return 생성된 댓글의 ID
     * @throws ResourceNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional
    public Long createComment(Long userId, CommentRequestDto requestDto) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user", "id", userId));

        // 게시글 조회
        Post post = postRepository.findById(requestDto.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("post", "id", requestDto.getPostId()));

        // 댓글 엔티티 생성
        Comment comment = Comment.builder()
                .content(requestDto.getContent())
                .build();

        // 사용자, 게시글 연결
        comment.setUser(user);
        comment.setPost(post);

        // 댓글 저장
        Comment savedComment = commentRepository.save(comment);
        return savedComment.getId();
    }

    /**
     * 특정 게시글의 댓글 목록 조회
     *
     * @param postId 게시글 ID
     * @return 댓글 목록
     * @throws ResourceNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        // 게시글 존재 여부 확인
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("post", "id", postId);
        }

        // 게시글의 댓글 목록 조회 후 DTO로 변환
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId).stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 댓글 수정
     *
     * @param userId    수정 요청자 ID
     * @param commentId 수정할 댓글 ID
     * @param content   수정할 내용
     * @return 수정된 댓글 정보
     * @throws ResourceNotFoundException 댓글을 찾을 수 없는 경우
     * @throws UnauthorizedException     댓글 작성자가 아닌 경우
     */
    @Transactional
    public Map<String, Object> updateComment(Long userId, Long commentId, String content) {
        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("comment", "id", commentId));

        // 작성자 권한 검증
        if (!comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("댓글 수정 권한이 없습니다.");
        }

        // 댓글 내용 업데이트
        comment.updateContent(content);
        Comment updatedComment = commentRepository.save(comment);

        // 응답 데이터 생성
        Map<String, Object> response = new HashMap<>();
        response.put("comment_id", updatedComment.getId());
        response.put("content", updatedComment.getContent());

        return response;
    }

    /**
     * 댓글 삭제
     *
     * @param userId    삭제 요청자 ID
     * @param commentId 삭제할 댓글 ID
     * @throws ResourceNotFoundException 댓글을 찾을 수 없는 경우
     * @throws UnauthorizedException     댓글 작성자가 아닌 경우
     */
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("comment", "id", commentId));

        // 작성자 권한 검증
        if (!comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("댓글 삭제 권한이 없습니다.");
        }

        // 댓글 삭제
        commentRepository.delete(comment);
    }

    /**
     * 특정 게시글의 댓글 수 조회
     *
     * @param postId 게시글 ID
     * @return 댓글 수
     */
    @Transactional(readOnly = true)
    public Long countCommentsByPostId(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    /**
     * 댓글을 Map 형태로 변환
     *
     * @param comment 변환할 댓글
     * @return 변환된 Map
     */
    private Map<String, Object> convertCommentToMap(Comment comment) {
        Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("comment_id", comment.getId());
        commentMap.put("user_id", comment.getUser().getId());
        commentMap.put("post_id", comment.getPost().getId());
        commentMap.put("comment_body", comment.getContent());
        commentMap.put("created_at", comment.getCreatedAt());
        commentMap.put("updated_at", comment.getUpdatedAt());

        return commentMap;
    }
}