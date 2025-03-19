package com.ricklee.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 댓글 작성/수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class CommentRequestDto {

    @NotNull(message = "게시글 ID는 필수 항목입니다.")
    private Long postId;

    @NotBlank(message = "댓글 내용은 필수 입력 항목입니다.")
    private String content;
}