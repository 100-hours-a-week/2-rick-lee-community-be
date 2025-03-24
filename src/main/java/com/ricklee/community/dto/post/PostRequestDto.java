package com.ricklee.community.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 게시글 작성/수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class PostRequestDto {

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 26, message = "제목은 최대 26자까지 입력 가능합니다.")
    private String title;

    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    private String content;

    // 이미지는 선택 사항
    private byte[] postImg;
}