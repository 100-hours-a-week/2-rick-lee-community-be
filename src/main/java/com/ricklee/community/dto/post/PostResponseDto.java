package com.ricklee.community.dto.post;

import com.ricklee.community.domain.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 게시글 응답 DTO
 * 클라이언트에 반환할 게시글 정보를 포함
 */
@Getter
@NoArgsConstructor
public class PostResponseDto {
    private Long postId;
    private String title;
    private String content;
    private byte[] postImg;
    private Integer viewCount;
    private String author;
    private Long commentCount;
    private Long likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 게시글 엔티티와 통계 정보를 받아 DTO로 변환
     * @param post 게시글 엔티티
     * @param commentCount 댓글 수
     * @param likeCount 좋아요 수
     */
    public PostResponseDto(Post post, Long commentCount, Long likeCount) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.postImg = post.getPostImg();
        this.viewCount = post.getViewCount();
        this.author = post.getUser().getNickname();
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
    }
}