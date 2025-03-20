package com.ricklee.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ricklee.community.dto.LoginRequestDto;
import com.ricklee.community.dto.SignupRequestDto;
import com.ricklee.community.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 API 테스트 - 성공")
    void signupApiSuccess() throws Exception {
        // given
        SignupRequestDto dto = new SignupRequestDto();
        dto.setEmail("test@example.com");
        dto.setPassword("Test1234!");
        dto.setNickname("testuser");

        // when & then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("register_success")))
                .andExpect(jsonPath("$.data.user_id", notNullValue()));
    }

    @Test
    @DisplayName("회원가입 API 테스트 - 유효성 검사 실패")
    void signupApiValidationFail() throws Exception {
        // given
        SignupRequestDto dto = new SignupRequestDto();
        dto.setEmail("not-an-email");
        dto.setPassword("short");
        dto.setNickname("t"); // 닉네임 너무 짧음

        // when & then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 후 로그인 API 흐름 테스트")
    void signupAndLoginFlow() throws Exception {
        // given - 회원가입 데이터
        SignupRequestDto signupDto = new SignupRequestDto();
        signupDto.setEmail("flow@example.com");
        signupDto.setPassword("FlowTest123!");
        signupDto.setNickname("flowuser");

        // 회원가입 실행
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDto)))
                .andExpect(status().isCreated());

        // 로그인 데이터
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("flow@example.com");
        loginDto.setPassword("FlowTest123!");

        // when & then - 로그인 실행 및 검증
        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("login_success")))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.user_id", notNullValue()));
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 API 테스트")
    void signupWithDuplicateEmail() throws Exception {
        // given - 첫번째 회원가입
        SignupRequestDto dto1 = new SignupRequestDto();
        dto1.setEmail("duplicate@example.com");
        dto1.setPassword("Test1234!");
        dto1.setNickname("user1");

        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        // 같은 이메일로 두번째 회원가입
        SignupRequestDto dto2 = new SignupRequestDto();
        dto2.setEmail("duplicate@example.com"); // 같은 이메일
        dto2.setPassword("Test5678!");
        dto2.setNickname("user2");

        // when & then
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("잘못된 인증으로 로그인 API 테스트")
    void loginWithInvalidCredentials() throws Exception {
        // given - 회원가입
        SignupRequestDto signupDto = new SignupRequestDto();
        signupDto.setEmail("valid@example.com");
        signupDto.setPassword("Valid123!");
        signupDto.setNickname("validuser");

        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDto)))
                .andExpect(status().isCreated());

        // 잘못된 비밀번호로 로그인
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("valid@example.com");
        loginDto.setPassword("WrongPass123!");

        // when & then
        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("authentication_failed")));
    }
}