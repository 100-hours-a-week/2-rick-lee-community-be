package com.ricklee.community.repository;

import com.ricklee.community.domain.Like;
import com.ricklee.community.domain.Post;
import com.ricklee.community.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 좋아요 Repository 인터페이스
 */
@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    /**
     * 특정 사용자가 특정 게시글에 누른 좋아요 조회
     * @param user 사용자
     * @param post 게시글
     * @return 좋아요 (Optional)
     */
    Optional<Like> findByUserAndPost(User user, Post post);

    /**
     * 특정 게시글의 좋아요 수 조회
     * @param postId 게시글 ID
     * @return 좋아요 수
     */
    @Query("SELECT COUNT(l) FROM Like l WHERE l.post.id = :postId")
    Long countByPostId(@Param("postId") Long postId);

    /**
     * 특정 사용자가 좋아요 누른 게시글 목록 조회
     * @param userId 사용자 ID
     * @return 좋아요 목록
     */
    List<Like> findByUserId(Long userId);

    /**
     * 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     * @return 좋아요 여부
     */
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    /**
     * 특정 사용자가 누른 좋아요 삭제
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     */
    void deleteByUserIdAndPostId(Long userId, Long postId);

    /**
     * 특정 게시글의 모든 좋아요 삭제 (게시글 삭제 시 사용)
     * @param postId 게시글 ID
     */
    void deleteAllByPostId(Long postId);
}