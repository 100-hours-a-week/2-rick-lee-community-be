package com.ricklee.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 비밀번호 변경 요청 DTO
 * 현재 비밀번호 일치 여부는 Service 레이어에서 검증
 */
@Getter
@Setter
@NoArgsConstructor
public class PasswordChangeRequestDto {

    @NotBlank(message = "현재 비밀번호는 필수 입력 항목입니다.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상, 20자 이하로 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
            message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개씩 포함해야 합니다."
    )
    private String newPassword;
}