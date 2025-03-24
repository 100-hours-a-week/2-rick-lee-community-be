package com.ricklee.community.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글 통계 정보를 담는 DTO
 * 게시글 ID, 댓글 수, 좋아요 수 등을 포함
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostStatsDto {
    private Long postId;
    private Long commentCount;
    private Long likeCount;
}