package com.ricklee.community.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PasswordEncoderTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("비밀번호 암호화 및 검증 테스트")
    void passwordEncodingAndMatching() {
        // given
        String rawPassword = "MySecurePassword123!";

        // when
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // then
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    @DisplayName("같은 비밀번호도 다른 해시값 생성 확인 (salt 검증)")
    void differentHashesForSamePassword() {
        // given
        String password = "SamePassword123!";

        // when
        String firstHash = passwordEncoder.encode(password);
        String secondHash = passwordEncoder.encode(password);

        // then
        assertNotEquals(firstHash, secondHash);
        assertTrue(passwordEncoder.matches(password, firstHash));
        assertTrue(passwordEncoder.matches(password, secondHash));
    }

    @Test
    @DisplayName("잘못된 비밀번호는 매치되지 않음 확인")
    void wrongPasswordDoesNotMatch() {
        // given
        String correctPassword = "CorrectPassword123!";
        String wrongPassword = "WrongPassword123!";

        // when
        String encodedPassword = passwordEncoder.encode(correctPassword);

        // then
        assertFalse(passwordEncoder.matches(wrongPassword, encodedPassword));
    }

    @Test
    @DisplayName("BCrypt 해시 구조 확인")
    void verifyBCryptHashFormat() {
        // given
        String password = "TestPassword123!";

        // when
        String encoded = passwordEncoder.encode(password);

        // then
        assertTrue(encoded.startsWith("$2a$"));  // BCrypt 알고리즘 prefix

        String[] parts = encoded.split("\\$");
        assertEquals(4, parts.length);  // $2a$10$randomSaltAndHash 형태
    }
}