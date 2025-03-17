package com.ricklee.community.service;

import com.ricklee.community.dto.LikeStatsDto;
import com.ricklee.community.domain.Like;
import com.ricklee.community.domain.Post;
import com.ricklee.community.domain.User;
import com.ricklee.community.repository.LikeRepository;
import com.ricklee.community.repository.PostRepository;
import com.ricklee.community.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 좋아요 관련 비즈니스 로직 처리 서비스
 */
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public LikeService(LikeRepository likeRepository, PostRepository postRepository, UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /**
     * 게시글 좋아요 추가
     *
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     * @return 처리 결과 메시지
     */
    @Transactional
    public String addLike(Long userId, Long postId) {
        // 이미 좋아요를 눌렀는지 확인
        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            return "이미 좋아요를 누른 게시글입니다.";
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 좋아요 생성 및 저장
        Like like = Like.createLike(user, post);
        likeRepository.save(like);

        return "좋아요가 추가되었습니다.";
    }

    /**
     * 게시글 좋아요 취소
     *
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     * @return 처리 결과 메시지
     */
    @Transactional
    public String removeLike(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        Like like = likeRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new RuntimeException("좋아요 정보를 찾을 수 없습니다."));

        likeRepository.delete(like);
        return "좋아요가 취소되었습니다.";
    }

    /**
     * 게시글 좋아요 통계 조회
     *
     * @param postId 게시글 ID
     * @param userId 현재 사용자 ID
     * @return 좋아요 통계 정보
     */
    @Transactional(readOnly = true)
    public LikeStatsDto getLikeStats(Long postId, Long userId) {
        // 좋아요 수 조회
        Long likeCount = likeRepository.countByPostId(postId);

        // 현재 사용자의 좋아요 여부 확인
        Boolean userLiked = false;
        if (userId != null) {
            userLiked = likeRepository.existsByUserIdAndPostId(userId, postId);
        }

        return new LikeStatsDto(postId, likeCount, userLiked);
    }
}