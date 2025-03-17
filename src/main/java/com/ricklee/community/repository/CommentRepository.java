package com.ricklee.community.repository;

import com.ricklee.community.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 댓글 Repository 인터페이스
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 특정 게시글에 달린 모든 댓글 조회 (생성일 기준 내림차순)
     * @param postId 게시글 ID
     * @return 댓글 목록
     */
    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);

    /**
     * 특정 게시글에 달린 댓글 페이징 조회
     * @param postId 게시글 ID
     * @param pageable 페이징 정보
     * @return 댓글 페이지
     */
    Page<Comment> findByPostId(Long postId, Pageable pageable);

    /**
     * 특정 게시글에 달린 댓글 수 조회
     * @param postId 게시글 ID
     * @return 댓글 수
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    Long countByPostId(@Param("postId") Long postId);

    /**
     * 특정 사용자가 작성한 댓글 조회 (생성일 기준 내림차순)
     * @param userId 사용자 ID
     * @return 댓글 목록
     */
    List<Comment> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 특정 부모 댓글에 달린 모든 대댓글 조회
     * @param parentCommentId 부모 댓글 ID
     * @return 대댓글 목록
     */
    List<Comment> findByParentCommentId(Long parentCommentId);

    /**
     * 특정 게시글의 최상위 댓글만 조회 (부모 댓글이 없는 댓글)
     * @param postId 게시글 ID
     * @return 최상위 댓글 목록
     */
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findTopLevelCommentsByPostId(@Param("postId") Long postId);
}