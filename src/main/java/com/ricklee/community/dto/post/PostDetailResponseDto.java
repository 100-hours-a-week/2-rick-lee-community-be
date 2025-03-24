package com.ricklee.community.dto.post;

import com.ricklee.community.domain.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 게시글 상세 정보를 담는 DTO
 * 게시글 내용, 통계 정보, 사용자 상호작용 정보를 포함
 */
@Getter
@NoArgsConstructor
public class PostDetailResponseDto {
    // 게시글 기본 정보
    private Long postId;
    private String title;
    private String content;
    private byte[] postImg;
    private Integer viewCount;

    // 작성자 정보
    private Long authorId;
    private String authorNickname;
    private byte[] authorProfileImg;

    // 통계 정보
    private Long commentCount;
    private Long likeCount;

    // 사용자 상호작용 정보
    private Boolean userLiked; // 현재 사용자의 좋아요 여부

    // 시간 정보
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 게시글 및 관련 정보를 통합한 상세 응답 DTO 생성
     *
     * @param post 게시글 엔티티
     * @param commentCount 댓글 수
     * @param likeCount 좋아요 수
     * @param userLiked 현재 사용자의 좋아요 여부
     */
    public PostDetailResponseDto(Post post, Long commentCount, Long likeCount, Boolean userLiked) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.postImg = post.getPostImg();
        this.viewCount = post.getViewCount();

        this.authorId = post.getUser().getId();
        this.authorNickname = post.getUser().getNickname();
        this.authorProfileImg = post.getUser().getProfileImg();

        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.userLiked = userLiked;

        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
    }
}