package com.ricklee.community.service;

import com.ricklee.community.domain.Like;
import com.ricklee.community.domain.Post;
import com.ricklee.community.domain.User;
import com.ricklee.community.dto.LikeStatsDto;
import com.ricklee.community.exception.BusinessException;
import com.ricklee.community.exception.ResourceNotFoundException;
import com.ricklee.community.repository.LikeRepository;
import com.ricklee.community.repository.PostRepository;
import com.ricklee.community.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 좋아요 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    /**
     * 생성자 주입을 통한 의존성 주입
     */
    public LikeService(LikeRepository likeRepository, UserRepository userRepository, PostRepository postRepository) {
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    /**
     * 게시글에 좋아요 추가
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     * @return 생성된 좋아요의 ID
     * @throws ResourceNotFoundException 사용자 또는 게시글을 찾을 수 없는 경우
     * @throws BusinessException 이미 좋아요를 누른 경우
     */
    @Transactional
    public Long addLike(Long userId, Long postId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user", "id", userId));

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("post", "id", postId));

        // 이미 좋아요를 눌렀는지 확인
        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new BusinessException("duplicate_like");
        }

        // 좋아요 생성
        Like like = Like.createLike(user, post);
        Like savedLike = likeRepository.save(like);

        return savedLike.getId();
    }

    /**
     * 게시글 좋아요 취소
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     * @return 삭제된 좋아요의 ID
     * @throws ResourceNotFoundException 사용자, 게시글 또는 좋아요를 찾을 수 없는 경우
     */
    @Transactional
    public Long removeLike(Long userId, Long postId) {
        // 사용자 조회
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user", "id", userId));

        // 게시글 조회
        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("post", "id", postId));

        // 좋아요 조회
        Like like = likeRepository.findByUserAndPost(
                        userRepository.getReferenceById(userId),
                        postRepository.getReferenceById(postId)
                )
                .orElseThrow(() -> new ResourceNotFoundException("like", "postId", postId));

        Long likeId = like.getId();

        // 좋아요 삭제
        likeRepository.delete(like);

        return likeId;
    }

    /**
     * 게시글 좋아요 통계 조회
     * @param postId 게시글 ID
     * @param userId 현재 사용자 ID (선택 사항)
     * @return 좋아요 통계 정보
     * @throws ResourceNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public LikeStatsDto getLikeStats(Long postId, Long userId) {
        // 게시글 존재 여부 확인
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("post", "id", postId);
        }

        // 좋아요 수 조회
        Long likeCount = likeRepository.countByPostId(postId);

        // 현재 사용자의 좋아요 여부 확인
        Boolean userLiked = false;
        if (userId != null) {
            userLiked = likeRepository.existsByUserIdAndPostId(userId, postId);
        }

        return new LikeStatsDto(postId, likeCount, userLiked);
    }

    /**
     * 특정 게시글의 좋아요 수 조회
     * @param postId 게시글 ID
     * @return 좋아요 수
     */
    @Transactional(readOnly = true)
    public Long countLikesByPostId(Long postId) {
        return likeRepository.countByPostId(postId);
    }

    /**
     * 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     * @return 좋아요 여부
     */
    @Transactional(readOnly = true)
    public boolean hasUserLikedPost(Long userId, Long postId) {
        return likeRepository.existsByUserIdAndPostId(userId, postId);
    }
}