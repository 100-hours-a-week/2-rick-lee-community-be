package com.ricklee.community.controller;

import com.ricklee.community.dto.LoginRequestDto;
import com.ricklee.community.dto.PasswordChangeRequestDto;
import com.ricklee.community.dto.SignupRequestDto;
import com.ricklee.community.dto.UserUpdateRequestDto;
import com.ricklee.community.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 회원가입 API
     * POST /users/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody SignupRequestDto requestDto) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = userService.signup(requestDto);

            Map<String, Object> data = new HashMap<>();
            data.put("user_id", userId);

            response.put("message", "register_success");
            response.put("data", data);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("message", "invalid_request");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 로그인 API
     * POST /users/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequestDto requestDto) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> loginResult = userService.login(requestDto);

            response.put("message", "login_success");
            response.put("data", loginResult);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", "invalid_credentials");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("message", "invalid_request");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 회원 정보 수정 API
     * PUT /users/{userId}
     */
    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateUserInfo(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequestDto requestDto) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.updateUserInfo(userId, requestDto);

            response.put("message", "user_updated");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", "invalid_request");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("message", "internal_server_error");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 비밀번호 변경 API
     * PUT /users/{userId}/password
     */
    @PutMapping("/{userId}/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @PathVariable Long userId,
            @Valid @RequestBody PasswordChangeRequestDto requestDto) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.changePassword(userId, requestDto);

            response.put("message", "password_updated");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", "invalid_request");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("message", "internal_server_error");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 회원 탈퇴 API
     * DELETE /users
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteUser(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = userService.getUserIdFromToken(token.replace("Bearer ", ""));
            userService.deleteUser(userId);

            response.put("message", "user_deleted");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", "unauthorized");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("message", "internal_server_error");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}