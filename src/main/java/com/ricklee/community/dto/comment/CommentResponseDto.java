package com.ricklee.community.dto.comment;

import com.ricklee.community.domain.Comment;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CommentResponseDto {
    // 댓글 기본 정보
    private Long commentId;
    private Long postId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 작성자 정보
    private Long authorId;
    private String authorNickname;
    private String authorProfileImgUrl;

    public CommentResponseDto(Comment comment) {
        this.commentId = comment.getId();
        this.postId = comment.getPost().getId();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();

        // 작성자 정보 추가
        this.authorId = comment.getUser().getId();
        this.authorNickname = comment.getUser().getNickname();
        this.authorProfileImgUrl = comment.getUser().getProfileImgUrl();
    }
}