package com.ricklee.community.repository;

import com.ricklee.community.domain.Like;
import com.ricklee.community.domain.LikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 좋아요 Repository 인터페이스
 */
@Repository
public interface LikeRepository extends JpaRepository<Like, LikeId> {

    /**
     * 특정 게시글의 좋아요 수 조회
     * @param postId 게시글 ID
     * @return 좋아요 수
     */
    @Query("SELECT COUNT(l) FROM Like l WHERE l.post.id = :postId")
    Long countByPostId(@Param("postId") Long postId);

    /**
     * 특정 사용자가 좋아요 누른 게시글 수 조회
     * @param userId 사용자 ID
     * @return 좋아요 누른 게시글 수
     */
    @Query("SELECT COUNT(l) FROM Like l WHERE l.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
}