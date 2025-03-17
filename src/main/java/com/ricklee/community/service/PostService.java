package com.ricklee.community.service;

import com.ricklee.community.dto.PostDetailResponseDto;
import com.ricklee.community.dto.PostListItemDto;
import com.ricklee.community.dto.PostStatsDto;
import com.ricklee.community.domain.Post;
import com.ricklee.community.domain.User;
import com.ricklee.community.repository.CommentRepository;
import com.ricklee.community.repository.LikeRepository;
import com.ricklee.community.repository.PostRepository;
import com.ricklee.community.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository,
                       CommentRepository commentRepository, LikeRepository likeRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
    }

    /**
     * 게시글 상세 조회 및 통계 정보, 사용자 상호작용 정보 포함
     * @param postId 게시글 ID
     * @param userId 현재 사용자 ID (로그인한 경우)
     * @return 게시글 상세 응답 DTO
     */
    @Transactional
    public PostDetailResponseDto getPostDetail(Long postId, Long userId) {
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 조회수 증가
        post.increaseViewCount();
        postRepository.save(post);

        // 댓글 수와 좋아요 수 조회
        Long commentCount = commentRepository.countByPostId(postId);
        Long likeCount = likeRepository.countByPostId(postId);

        // 현재 사용자의 좋아요 여부 확인
        Boolean userLiked = false;
        if (userId != null) {
            userLiked = likeRepository.existsByUserIdAndPostId(userId, postId);
        }

        // DTO로 변환하여 반환
        return new PostDetailResponseDto(post, commentCount, likeCount, userLiked);
    }

    /**
     * 게시글 목록 조회 (각 게시글의 댓글 수, 좋아요 수 포함)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 게시글 목록 DTO
     */
    @Transactional(readOnly = true)
    public List<PostListItemDto> getPostList(int page, int size) {
        // 페이징 처리된 게시글 목록 조회
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> postPage = postRepository.findAll(pageable);

        // 각 게시글에 대한 통계 정보를 포함한 DTO 생성
        return postPage.getContent().stream()
                .map(post -> {
                    Long commentCount = commentRepository.countByPostId(post.getId());
                    Long likeCount = likeRepository.countByPostId(post.getId());
                    return new PostListItemDto(post, commentCount, likeCount);
                })
                .collect(Collectors.toList());
    }

    // 게시글 생성, 수정, 삭제 등 다른 메서드들...
}