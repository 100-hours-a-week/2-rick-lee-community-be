package com.ricklee.community.service;

import com.ricklee.community.domain.User;
import com.ricklee.community.dto.user.LoginRequestDto;
import com.ricklee.community.dto.user.SignupRequestDto;
import com.ricklee.community.exception.custom.DuplicateResourceException;
import com.ricklee.community.exception.custom.ResourceNotFoundException;
import com.ricklee.community.exception.custom.UnauthorizedException;
import com.ricklee.community.repository.UserRepository;
import com.ricklee.community.util.jwt.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    private SignupRequestDto signupRequestDto;
    private User user;

    @BeforeEach
    void setUp() {


        // 테스트 데이터 생성
        signupRequestDto = new SignupRequestDto();
        signupRequestDto.setEmail("test@example.com");
        signupRequestDto.setPassword("Test1234!");
        signupRequestDto.setNickname("testuser");

        user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("testuser")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    @Test
    @Commit
    @DisplayName("회원가입 서비스 - 성공")
    void signupSuccess() {
        // given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByNickname(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // when
        Long userId = userService.signup(signupRequestDto);

        // then
        assertEquals(1L, userId);
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByNickname("testuser");
        verify(passwordEncoder).encode("Test1234!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 서비스 - 이메일 중복으로 실패")
    void signupFailDueToEmailDuplicate() {
        // given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // when & then
        assertThrows(DuplicateResourceException.class, () -> {
            userService.signup(signupRequestDto);
        });
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 서비스 - 닉네임 중복으로 실패")
    void signupFailDueToNicknameDuplicate() {
        // given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByNickname("testuser")).thenReturn(true);

        // when & then
        assertThrows(DuplicateResourceException.class, () -> {
            userService.signup(signupRequestDto);
        });
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByNickname("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 서비스 - 성공")
    void loginSuccess() {
        when(jwtUtil.generateToken(eq(1L), eq("MEMBER"))).thenReturn("mocked-jwt-token");

        // given
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("test@example.com");
        loginRequestDto.setPassword("Test1234!");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Test1234!", "encodedPassword")).thenReturn(true);

        // when
        Map<String, Object> result = userService.login(loginRequestDto);

        // then
        assertNotNull(result);
        assertNotNull(result.get("token"));
        assertEquals(1L, result.get("user_id"));
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("Test1234!", "encodedPassword");
    }

    @Test
    @DisplayName("로그인 서비스 - 사용자 없음")
    void loginFailUserNotFound() {
        // given
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("nonexistent@example.com");
        loginRequestDto.setPassword("Test1234!");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // when & then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.login(loginRequestDto);
        });
        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 서비스 - 비밀번호 불일치")
    void loginFailPasswordMismatch() {
        // given
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("test@example.com");
        loginRequestDto.setPassword("WrongPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "encodedPassword")).thenReturn(false);

        // when & then
        assertThrows(UnauthorizedException.class, () -> {
            userService.login(loginRequestDto);
        });
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("WrongPassword", "encodedPassword");
    }

    @Test
    @DisplayName("JWT 토큰 검증")
    void getUserIdFromTokenTest() {
        // given
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("test@example.com");
        loginRequestDto.setPassword("Test1234!");

        // 사용자 인증 설정
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Test1234!", "encodedPassword")).thenReturn(true);

        // 중요: login 메서드가 반환할 토큰 지정
        String testToken = "test-token";
        when(jwtUtil.generateToken(1L, "MEMBER")).thenReturn(testToken);

        // 중요: 토큰 검증 설정
        when(jwtUtil.getUserIdFromToken(testToken)).thenReturn(1L);

        // 로그인으로 토큰 생성
        Map<String, Object> loginResult = userService.login(loginRequestDto);
        String token = (String) loginResult.get("token");

        // 여기서 token은 "test-token"이어야 함
        assertEquals(testToken, token);

        // when
        Long extractedUserId = userService.getUserIdFromToken(token);

        // then
        assertEquals(1L, extractedUserId);
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증")
    void invalidTokenTest() {
        // given
        String invalidToken = "invalid_token";

        // JwtUtil이 예외를 던지도록 설정
        when(jwtUtil.getUserIdFromToken(invalidToken)).thenThrow(new JwtException("Invalid token"));

        // when & then
        assertThrows(UnauthorizedException.class, () -> {
            userService.getUserIdFromToken(invalidToken);
        });
    }
}