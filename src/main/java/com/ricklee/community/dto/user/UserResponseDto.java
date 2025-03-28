package com.ricklee.community.dto.user;

import com.ricklee.community.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보 응답 DTO
 * 사용자 관련 API 응답에 사용되는 통합 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private Long id;
    private String email;
    private String nickname;
    private byte[] profileImg;
    private String profileImgUrl;

    /**
     * User 엔티티에서 UserResponseDto 객체 생성
     *
     * @param user User 엔티티
     * @return UserResponseDto 객체
     */
    public static UserResponseDto from(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImg(user.getProfileImg())
                .profileImgUrl(user.getProfileImg() != null ?
                        "/api/users/" + user.getId() + "/profile-image" : null)
                .build();
    }
}