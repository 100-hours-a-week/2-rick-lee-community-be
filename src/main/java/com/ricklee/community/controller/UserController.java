package com.ricklee.community.controller;

import com.ricklee.community.dto.common.ApiResponse;
import com.ricklee.community.dto.user.*;
import com.ricklee.community.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 관련 API를 처리하는 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * 회원 정보 조회 API
     * GET /users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserInfo(@PathVariable Long userId) {
        UserResponseDto userInfo = userService.getUserInfo(userId);

        return ResponseEntity
                .ok(ApiResponse.success("user_found", userInfo));
    }
    /**
     * 회원가입 API
     * POST /users/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Map<String, Long>>> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        Long userId = userService.signup(requestDto);

        Map<String, Long> data = new HashMap<>();
        data.put("user_id", userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("register_success", data));
    }

    /**
     * 로그인 API
     * POST /users/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequestDto requestDto) {
        Map<String, Object> loginResult = userService.login(requestDto);

        return ResponseEntity
                .ok(ApiResponse.success("login_success", loginResult));
    }

    /**
     * 닉네임 중복 확인 API
     * GET /users/check-nickname
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestParam String nickname) {
        // 닉네임 유효성 검사
        if (nickname == null || nickname.trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("invalid_input", "닉네임이 비어있습니다."));
        }

        // 닉네임 중복 확인
        boolean isAvailable = userService.isNicknameAvailable(nickname);

        if (isAvailable) {
            return ResponseEntity
                    .ok(ApiResponse.success("nickname_available", true));
        } else {
            return ResponseEntity
                    .ok(ApiResponse.error("duplicate_nickname", "이미 사용 중인 닉네임입니다."));
        }
    }

    /**
     * 회원 정보 수정 API
     * PUT /users/{userId}
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> updateUserInfo(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequestDto requestDto) {
        userService.updateUserInfo(userId, requestDto);

        return ResponseEntity
                .ok(ApiResponse.success("user_updated"));
    }

    /**
     * 비밀번호 변경 API
     * PUT /users/{userId}/password
     */
    @PutMapping("/{userId}/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long userId,
            @Valid @RequestBody PasswordChangeRequestDto requestDto) {
        userService.changePassword(userId, requestDto);

        return ResponseEntity
                .ok(ApiResponse.success("password_updated"));
    }

    /**
     * 회원 탈퇴 API
     * DELETE /users
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteUser(@RequestHeader("Authorization") String token) {
        Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
        userService.deleteUser(userId);

        return ResponseEntity
                .ok(ApiResponse.success("user_deleted"));
    }
}