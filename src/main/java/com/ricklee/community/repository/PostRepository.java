package com.ricklee.community.repository;

import com.ricklee.community.domain.Post;
import com.ricklee.community.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 게시글 Repository 인터페이스
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 특정 사용자가 작성한 게시글 목록 조회
     * @param user 사용자
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    Page<Post> findByUser(User user, Pageable pageable);

    /**
     * 제목에 특정 키워드가 포함된 게시글 검색
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    Page<Post> findByTitleContaining(String keyword, Pageable pageable);

    /**
     * 내용에 특정 키워드가 포함된 게시글 검색
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    Page<Post> findByContentContaining(String keyword, Pageable pageable);

    /**
     * 제목 또는 내용에 특정 키워드가 포함된 게시글 검색
     * @param titleKeyword 제목 검색 키워드
     * @param contentKeyword 내용 검색 키워드
     * @param pageable 페이징 정보
     * @return 게시글 페이지
     */
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:titleKeyword% OR p.content LIKE %:contentKeyword%")
    Page<Post> findByTitleContainingOrContentContaining(
            @Param("titleKeyword") String titleKeyword,
            @Param("contentKeyword") String contentKeyword,
            Pageable pageable);

    /**
     * 엔티티 그래프를 사용하여 게시글과 작성자 정보를 함께 조회
     * @param id 게시글 ID
     * @return 게시글 (Optional)
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.id = :id")
    Optional<Post> findWithUserById(@Param("id") Long id);

    /**
     * 게시글 ID로 조회수 증가
     * @param id 게시글 ID
     */
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);
}