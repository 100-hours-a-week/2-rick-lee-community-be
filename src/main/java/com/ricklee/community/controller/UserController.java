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
 * RESTful API 설계 원칙을 따름
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입 API
     * POST /users
     */
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<Map<String, Long>>> createUser(@Valid @RequestBody SignupRequestDto requestDto) {
        Long userId = userService.signup(requestDto);

        Map<String, Long> data = new HashMap<>();
        data.put("user_id", userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("user_created", data));
    }

    /**
     * 로그인 API
     * POST /auth/login
     */
    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequestDto requestDto) {
        Map<String, Object> loginResult = userService.login(requestDto);

        return ResponseEntity
                .ok(ApiResponse.success("login_success", loginResult));
    }
    
    /**
     * 사용자 정보 조회 API
     * GET /users/{userId}
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUser(@PathVariable Long userId) {
        UserResponseDto userResponseDto = userService.getUserInfo(userId);
        return ResponseEntity
                .ok(ApiResponse.success("user_found", userResponseDto));
    }

    /**
     * 회원 정보 수정 API
     * PUT /users/{userId}
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequestDto requestDto) {
        userService.updateUserInfo(userId, requestDto);

        return ResponseEntity
                .ok(ApiResponse.success("user_updated"));
    }

    /**
     * 비밀번호 변경 API
     * PATCH /users/{userId}/password
     */
    @PatchMapping("/users/{userId}/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @PathVariable Long userId,
            @Valid @RequestBody PasswordChangeRequestDto requestDto) {
        userService.changePassword(userId, requestDto);

        return ResponseEntity
                .ok(ApiResponse.success("password_updated"));
    }

    /**
     * 회원 탈퇴 API
     * DELETE /users/{userId}
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);

        return ResponseEntity
                .ok(ApiResponse.success("user_deleted"));
    }
}