package com.ricklee.community.dto.like;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글 좋아요 통계 정보를 담는 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LikeStatsDto {
    private Long postId;
    private Long likeCount;
    private Boolean userLiked; // 현재 사용자의 좋아요 여부
}