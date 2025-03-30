package com.ricklee.community.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostRequestDto {

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(min = 1, max = 100, message = "제목은 1자 이상, 100자 이하로 입력해주세요.")
    private String title;

    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    private String content;

    private String postImgUrl;
}