package com.ricklee.community.service;

import com.ricklee.community.domain.LikeId;
import com.ricklee.community.domain.Post;
import com.ricklee.community.domain.User;
import com.ricklee.community.dto.post.PostDetailResponseDto;
import com.ricklee.community.dto.post.PostListItemDto;
import com.ricklee.community.dto.post.PostRequestDto;
import com.ricklee.community.exception.custom.ResourceNotFoundException;
import com.ricklee.community.exception.custom.UnauthorizedException;
import com.ricklee.community.repository.CommentRepository;
import com.ricklee.community.repository.LikeRepository;
import com.ricklee.community.repository.PostRepository;
import com.ricklee.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 게시글 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final LikeService likeService;
    private final ImageService imageService;

    /**
     * 게시글 작성
     * @param userId 작성자 ID
     * @param requestDto 게시글 작성 요청 정보
     * @param file 이미지 파일 (선택사항)
     * @return 생성된 게시글의 ID
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional
    public Long createPost(Long userId, PostRequestDto requestDto, MultipartFile file) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user", "id", userId));

        // 이미지 업로드 (있는 경우)
        String postImgUrl = null;
        if (file != null && !file.isEmpty()) {
            postImgUrl = imageService.uploadFile(file, "posts");
        }

        // 게시글 엔티티 생성
        Post post = Post.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .postImgUrl(postImgUrl)
                .build();

        // 사용자와 게시글 연결
        post.setUser(user);

        // 게시글 저장
        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }

    /**
     * 특정 게시글 상세 조회
     * @param postId 게시글 ID
     * @param userId 조회하는 사용자 ID
     * @return 게시글 상세 정보
     * @throws ResourceNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional
    public PostDetailResponseDto getPostDetail(Long postId, Long userId) {
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("post", "id", postId));

        // 조회수 증가
        post.increaseViewCount();
        postRepository.save(post);

        // 댓글 수와 좋아요 수 조회
        Long commentCount = commentRepository.countByPostId(postId);
        Long likeCount = likeRepository.countByPostId(postId);

        // 현재 사용자의 좋아요 여부 확인
        Boolean userLiked = false;
        if (userId != null) {
            LikeId likeId = new LikeId(userId, postId);
            userLiked = likeRepository.existsById(likeId);
        }

        // DTO로 변환하여 반환
        return new PostDetailResponseDto(post, commentCount, likeCount, userLiked);
    }

    /**
     * 게시글 목록 조회
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<PostListItemDto> getPostList(int page, int size) {
        // 페이징 처리된 게시글 목록 조회 (최신순)
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

    /**
     * 페이지네이션 정보 조회
     * @param page 현재 페이지 번호 (1부터 시작)
     * @param size 페이지 크기
     * @return 페이지네이션 정보
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPaginationInfo(int page, int size) {
        // 전체 게시글 수 조회
        long totalItems = postRepository.count();

        // 전체 페이지 수 계산
        int totalPages = (int) Math.ceil((double) totalItems / size);

        // 페이지네이션 정보 생성
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("per_page", size);
        pagination.put("total_pages", totalPages);
        pagination.put("total_items", totalItems);

        return pagination;
    }

    /**
     * 게시글 수정
     * @param userId 수정 요청자 ID
     * @param postId 수정할 게시글 ID
     * @param requestDto 게시글 수정 요청 정보
     * @param file 이미지 파일 (선택사항)
     * @return 수정된 게시글 정보
     * @throws ResourceNotFoundException 게시글을 찾을 수 없는 경우
     * @throws UnauthorizedException 게시글 작성자가 아닌 경우
     */
    @Transactional
    public Map<String, Object> updatePost(Long userId, Long postId, PostRequestDto requestDto, MultipartFile file) {
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("post", "id", postId));

        // 작성자 권한 검증
        if (!post.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("게시글 수정 권한이 없습니다.");
        }

        // 이미지 처리
        String postImgUrl = post.getPostImgUrl();
        if (file != null && !file.isEmpty()) {
            // 기존 이미지가 있으면 S3에서 삭제
            if (postImgUrl != null && !postImgUrl.isEmpty()) {
                imageService.deleteFile(postImgUrl);
            }

            // 새 이미지 업로드
            postImgUrl = imageService.uploadFile(file, "posts");
        }

        // 게시글 수정
        post.update(requestDto.getTitle(), requestDto.getContent(), postImgUrl);
        Post updatedPost = postRepository.save(post);

        // 응답 데이터 생성
        Map<String, Object> response = new HashMap<>();
        response.put("post_id", updatedPost.getId());
        response.put("title", updatedPost.getTitle());
        response.put("content", updatedPost.getContent());
        response.put("image_url", updatedPost.getPostImgUrl());
        response.put("updated_at", updatedPost.getUpdatedAt());

        return response;
    }

    /**
     * 게시글 삭제
     * @param userId 삭제 요청자 ID
     * @param postId 삭제할 게시글 ID
     * @throws ResourceNotFoundException 게시글을 찾을 수 없는 경우
     * @throws UnauthorizedException 게시글 작성자가 아닌 경우
     */
    @Transactional
    public void deletePost(Long userId, Long postId) {
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("post", "id", postId));

        // 작성자 권한 검증
        if (!post.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("게시글 삭제 권한이 없습니다.");
        }

        // 이미지가 있으면 S3에서 삭제
        if (post.getPostImgUrl() != null && !post.getPostImgUrl().isEmpty()) {
            imageService.deleteFile(post.getPostImgUrl());
        }

        // 게시글 삭제
        postRepository.delete(post);
    }

    /**
     * 게시글에 좋아요 추가
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     * @return 좋아요 수
     */
    @Transactional
    public Long addLike(Long userId, Long postId) {
        // LikeService를 통해 좋아요 추가
        likeService.addLike(userId, postId);

        // 좋아요 수 반환
        return likeRepository.countByPostId(postId);
    }

    /**
     * 게시글 좋아요 취소
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     * @return 남은 좋아요 수
     */
    @Transactional
    public Long removeLike(Long userId, Long postId) {
        // LikeService를 통해 좋아요 취소
        likeService.removeLike(userId, postId);

        // 남은 좋아요 수 반환
        return likeRepository.countByPostId(postId);
    }
}