package com.ricklee.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ricklee.community.controller.UserController;
import com.ricklee.community.dto.user.LoginRequestDto;
import com.ricklee.community.dto.user.PasswordChangeRequestDto;
import com.ricklee.community.dto.user.SignupRequestDto;
import com.ricklee.community.dto.user.UserUpdateRequestDto;
import com.ricklee.community.exception.custom.DuplicateResourceException;
import com.ricklee.community.exception.custom.ResourceNotFoundException;
import com.ricklee.community.exception.custom.UnauthorizedException;
import com.ricklee.community.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.HashMap;
import java.util.Map;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private final String VALID_TOKEN = "Bearer valid_token";
    private final Long VALID_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

        // 기본 토큰 검증 설정
        when(userService.getUserIdFromToken(VALID_TOKEN.replace("Bearer ", "")))
                .thenReturn(VALID_USER_ID);
    }

    @Test
    @DisplayName("회원가입 API 테스트 - 성공")
    void signupSuccess() throws Exception {
        // given
        SignupRequestDto requestDto = new SignupRequestDto();
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("Test1234!");
        requestDto.setNickname("testuser");

        when(userService.signup(any(SignupRequestDto.class))).thenReturn(1L);

        // when & then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("register_success")))
                .andExpect(jsonPath("$.data.user_id", is(1)));
    }

    @Test
    @DisplayName("회원가입 API 테스트 - 중복 이메일")
    void signupDuplicateEmail() throws Exception {
        // given
        SignupRequestDto requestDto = new SignupRequestDto();
        requestDto.setEmail("duplicate@example.com");
        requestDto.setPassword("Test1234!");
        requestDto.setNickname("testuser");

        when(userService.signup(any(SignupRequestDto.class)))
                .thenThrow(new DuplicateResourceException("user", "email", "duplicate@example.com"));

        // when & then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("duplicate_resource")));
    }

    @Test
    @DisplayName("로그인 API 테스트 - 성공")
    void loginSuccess() throws Exception {
        // given
        LoginRequestDto requestDto = new LoginRequestDto();
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("Test1234!");

        Map<String, Object> loginResult = new HashMap<>();
        loginResult.put("token", "jwt_token");
        loginResult.put("user_id", 1L);

        when(userService.login(any(LoginRequestDto.class))).thenReturn(loginResult);

        // when & then
        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("login_success")))
                .andExpect(jsonPath("$.data.token", is("jwt_token")))
                .andExpect(jsonPath("$.data.user_id", is(1)));
    }

    @Test
    @DisplayName("로그인 API 테스트 - 사용자 없음")
    void loginUserNotFound() throws Exception {
        // given
        LoginRequestDto requestDto = new LoginRequestDto();
        requestDto.setEmail("nonexistent@example.com");
        requestDto.setPassword("Test1234!");

        when(userService.login(any(LoginRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("user", "email", "nonexistent@example.com"));

        // when & then
        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("user_not_found")));
    }

    @Test
    @DisplayName("로그인 API 테스트 - 비밀번호 불일치")
    void loginWrongPassword() throws Exception {
        // given
        LoginRequestDto requestDto = new LoginRequestDto();
        requestDto.setEmail("test@example.com");
        requestDto.setPassword("WrongPassword!");

        when(userService.login(any(LoginRequestDto.class)))
                .thenThrow(new UnauthorizedException("비밀번호가 일치하지 않습니다."));

        // when & then
        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("invalid_credentials")));
    }

    @Test
    @DisplayName("회원 정보 수정 API 테스트 - 성공")
    void updateUserInfoSuccess() throws Exception {
        // given
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto();
        requestDto.setNickname("updatedNick");
        requestDto.setProfileImg(null);

        // when & then
        mockMvc.perform(put("/users/{userId}", VALID_USER_ID)
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("user_updated")));
    }

    @Test
    @DisplayName("회원 정보 수정 API 테스트 - 닉네임 중복")
    void updateUserInfoDuplicateNickname() throws Exception {
        // given
        UserUpdateRequestDto dto = new UserUpdateRequestDto();
        dto.setNickname("duplicateNick");
        dto.setProfileImg(null);

        String requestBody = objectMapper.writeValueAsString(dto);
        System.out.println("Request Body: " + requestBody); // 변환된 JSON 출력

        doThrow(new DuplicateResourceException("user", "nickname", "duplicateNick"))
                .when(userService).updateUserInfo(eq(VALID_USER_ID), any(UserUpdateRequestDto.class));

        // when & then
        mockMvc.perform(put("/users/{userId}", VALID_USER_ID)
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print()) // 상세 로그 출력
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("회원 정보 수정 API 테스트 - 사용자 없음")
    void updateUserInfoUserNotFound() throws Exception {
        // given
        UserUpdateRequestDto requestDto = new UserUpdateRequestDto();
        requestDto.setNickname("updatedNick");
        requestDto.setProfileImg(null);

        doThrow(new ResourceNotFoundException("user", "id", VALID_USER_ID))
                .when(userService).updateUserInfo(eq(VALID_USER_ID), any(UserUpdateRequestDto.class));

        // when & then
        mockMvc.perform(put("/users/{userId}", VALID_USER_ID)
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("user_not_found")));
    }

    @Test
    @DisplayName("비밀번호 변경 API 테스트 - 성공")
    void changePasswordSuccess() throws Exception {
        // given
        PasswordChangeRequestDto requestDto = new PasswordChangeRequestDto();
        requestDto.setCurrentPassword("CurrentPass123!");
        requestDto.setNewPassword("NewPass456!");

        // when & then
        mockMvc.perform(put("/users/{userId}/password", VALID_USER_ID)
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("password_updated")));
    }

    @Test
    @DisplayName("비밀번호 변경 API 테스트 - 현재 비밀번호 불일치")
    void changePasswordWrongCurrentPassword() throws Exception {
        // given
        PasswordChangeRequestDto requestDto = new PasswordChangeRequestDto();
        requestDto.setCurrentPassword("WrongCurrentPass!");
        requestDto.setNewPassword("NewPass456!");

        doThrow(new UnauthorizedException("현재 비밀번호가 일치하지 않습니다."))
                .when(userService).changePassword(eq(VALID_USER_ID), any(PasswordChangeRequestDto.class));

        // when & then
        mockMvc.perform(put("/users/{userId}/password", VALID_USER_ID)
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("invalid_credentials")));
    }

    @Test
    @DisplayName("비밀번호 변경 API 테스트 - 사용자 없음")
    void changePasswordUserNotFound() throws Exception {
        // given
        PasswordChangeRequestDto requestDto = new PasswordChangeRequestDto();
        requestDto.setCurrentPassword("CurrentPass123!");
        requestDto.setNewPassword("NewPass456!");

        doThrow(new ResourceNotFoundException("user", "id", VALID_USER_ID))
                .when(userService).changePassword(eq(VALID_USER_ID), any(PasswordChangeRequestDto.class));

        // when & then
        mockMvc.perform(put("/users/{userId}/password", VALID_USER_ID)
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("user_not_found")));
    }

    @Test
    @DisplayName("회원 탈퇴 API 테스트 - 성공")
    void deleteUserSuccess() throws Exception {
        // when & then
        mockMvc.perform(delete("/users")
                        .header("Authorization", VALID_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("user_deleted")));
    }

    @Test
    @DisplayName("회원 탈퇴 API 테스트 - 유효하지 않은 토큰")
    void deleteUserInvalidToken() throws Exception {
        // given
        String invalidToken = "Bearer invalid_token";
        when(userService.getUserIdFromToken(invalidToken.replace("Bearer ", "")))
                .thenThrow(new UnauthorizedException("유효하지 않은 토큰입니다."));

        // when & then
        mockMvc.perform(delete("/users")
                        .header("Authorization", invalidToken))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("unauthorized")));
    }
}