package com.ricklee.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원 정보 수정 요청 DTO
 * 닉네임 중복 확인은 Service 레이어에서 수행
 */
@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequestDto {

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2자 이상, 10자 이하로 입력해주세요.")
    private String nickname;

    // 프로필 이미지는 선택 사항
    private byte[] profileImg;
}