package com.ricklee.community.service;

import com.ricklee.community.domain.User;
import com.ricklee.community.dto.user.LoginRequestDto;
import com.ricklee.community.dto.user.SignupRequestDto;
import com.ricklee.community.exception.custom.DuplicateResourceException;
import com.ricklee.community.exception.custom.ResourceNotFoundException;
import com.ricklee.community.exception.custom.UnauthorizedException;
import com.ricklee.community.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private SignupRequestDto createSignupDto() {
        SignupRequestDto dto = new SignupRequestDto();
        dto.setEmail("test@example.com");
        dto.setPassword("Test1234!");
        dto.setNickname("testuser");
        return dto;
    }

    @BeforeEach
    void setUp() {
        // 테스트 시작 전 데이터 정리
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 데이터 정리
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signupSuccess() {
        // given
        SignupRequestDto dto = createSignupDto();

        // when
        Long userId = userService.signup(dto);

        // then
        assertNotNull(userId);
        assertTrue(userId > 0);

        // verify user in repository
        Optional<User> foundUser = userRepository.findById(userId);
        assertTrue(foundUser.isPresent());
        assertEquals(dto.getEmail(), foundUser.get().getEmail());
        assertEquals(dto.getNickname(), foundUser.get().getNickname());
        // password should be encoded and different from the input
        assertNotEquals(dto.getPassword(), foundUser.get().getPassword());
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시 예외 발생")
    void signupFailWithDuplicateEmail() {
        // given
        SignupRequestDto dto1 = createSignupDto();
        userService.signup(dto1);

        SignupRequestDto dto2 = createSignupDto();
        dto2.setNickname("anotherUser"); // 닉네임은 다름

        // when & then
        assertThrows(DuplicateResourceException.class, () -> {
            userService.signup(dto2);
        });
    }

    @Test
    @DisplayName("중복 닉네임으로 회원가입 시 예외 발생")
    void signupFailWithDuplicateNickname() {
        // given
        SignupRequestDto dto1 = createSignupDto();
        userService.signup(dto1);

        SignupRequestDto dto2 = createSignupDto();
        dto2.setEmail("another@example.com"); // 이메일은 다름

        // when & then
        assertThrows(DuplicateResourceException.class, () -> {
            userService.signup(dto2);
        });
    }

    @Test
    @DisplayName("회원가입 후 로그인 성공")
    void loginSuccessAfterSignup() {
        // given: sign up a user
        SignupRequestDto signupDto = createSignupDto();
        Long userId = userService.signup(signupDto);

        // create login request
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail(signupDto.getEmail());
        loginDto.setPassword(signupDto.getPassword());

        // when: login with credentials
        Map<String, Object> loginResult = userService.login(loginDto);

        // then: verify login result
        assertNotNull(loginResult);
        assertNotNull(loginResult.get("token"));
        assertEquals(userId, loginResult.get("user_id"));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 예외 발생")
    void loginFailWithWrongPassword() {
        // given: sign up a user
        SignupRequestDto signupDto = createSignupDto();
        userService.signup(signupDto);

        // create login request with wrong password
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail(signupDto.getEmail());
        loginDto.setPassword("WrongPass123!");

        // when & then: login should fail
        assertThrows(UnauthorizedException.class, () -> {
            userService.login(loginDto);
        });
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 예외 발생")
    void loginFailWithNonExistentEmail() {
        // given: login request for non-existent user
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("nonexistent@example.com");
        loginDto.setPassword("Test1234!");

        // when & then: login should fail
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.login(loginDto);
        });
    }

    @Test
    @DisplayName("JWT 토큰 생성 및 검증")
    void jwtTokenGenerationAndValidation() {
        // given: sign up and login to get token
        SignupRequestDto signupDto = createSignupDto();
        Long userId = userService.signup(signupDto);

        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail(signupDto.getEmail());
        loginDto.setPassword(signupDto.getPassword());

        Map<String, Object> loginResult = userService.login(loginDto);
        String token = (String) loginResult.get("token");

        // when: extract user id from token
        Long extractedUserId = userService.getUserIdFromToken(token);

        // then: verify extracted user id
        assertEquals(userId, extractedUserId);
    }
}