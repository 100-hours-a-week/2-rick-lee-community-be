package com.ricklee.community.service;

import com.ricklee.community.domain.Like;
import com.ricklee.community.domain.LikeId;
import com.ricklee.community.domain.Post;
import com.ricklee.community.domain.User;
import com.ricklee.community.dto.like.LikeStatsDto;
import com.ricklee.community.exception.custom.BusinessException;
import com.ricklee.community.exception.custom.ResourceNotFoundException;
import com.ricklee.community.repository.LikeRepository;
import com.ricklee.community.repository.PostRepository;
import com.ricklee.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 좋아요 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    /**
     * 게시글에 좋아요 추가
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     * @return 좋아요 추가 성공 메시지
     * @throws ResourceNotFoundException 사용자 또는 게시글을 찾을 수 없는 경우
     * @throws BusinessException 이미 좋아요를 누른 경우
     */
    @Transactional
    public String addLike(Long userId, Long postId) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user", "id", userId));

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("post", "id", postId));

        // 이미 좋아요를 눌렀는지 확인 (복합 키로 직접 조회)
        LikeId likeId = new LikeId(userId, postId);
        if (likeRepository.existsById(likeId)) {
            throw new BusinessException("duplicate_like");
        }

        // 좋아요 생성
        Like like = Like.createLike(user, post);
        likeRepository.save(like);

        return "좋아요가 추가되었습니다.";
    }

    /**
     * 게시글 좋아요 취소
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     * @return 좋아요 취소 성공 메시지
     * @throws ResourceNotFoundException 좋아요를 찾을 수 없는 경우
     */
    @Transactional
    public String removeLike(Long userId, Long postId) {
        // 사용자와 게시글 존재 여부 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user", "id", userId));

        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("post", "id", postId));

        // 복합 키로 직접 삭제
        LikeId likeId = new LikeId(userId, postId);
        if (!likeRepository.existsById(likeId)) {
            throw new ResourceNotFoundException("like", "userId,postId", userId + "," + postId);
        }

        // 좋아요 삭제
        likeRepository.deleteById(likeId);

        return "좋아요가 취소되었습니다.";
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
            LikeId likeId = new LikeId(userId, postId);
            userLiked = likeRepository.existsById(likeId);
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
        LikeId likeId = new LikeId(userId, postId);
        return likeRepository.existsById(likeId);
    }
}