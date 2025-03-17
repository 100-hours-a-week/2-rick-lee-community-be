package com.ricklee.community.dto;

import com.ricklee.community.domain.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 게시글 목록에서 사용되는 간략한 게시글 정보 DTO
 */
@Getter
@NoArgsConstructor
public class PostListItemDto {
    private Long postId;
    private String title;
    private String authorNickname;
    private Integer viewCount;
    private Long commentCount;
    private Long likeCount;
    private LocalDateTime createdAt;

    /**
     * 게시글 목록 아이템 DTO 생성
     *
     * @param post 게시글 엔티티
     * @param commentCount 댓글 수
     * @param likeCount 좋아요 수
     */
    public PostListItemDto(Post post, Long commentCount, Long likeCount) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.authorNickname = post.getUser().getNickname();
        this.viewCount = post.getViewCount();
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.createdAt = post.getCreatedAt();
    }
}