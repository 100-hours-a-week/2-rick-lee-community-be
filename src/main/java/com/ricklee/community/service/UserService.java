package com.ricklee.community.service;

import com.ricklee.community.domain.User;
import com.ricklee.community.dto.LoginRequestDto;
import com.ricklee.community.dto.PasswordChangeRequestDto;
import com.ricklee.community.dto.SignupRequestDto;
import com.ricklee.community.dto.UserUpdateRequestDto;
import com.ricklee.community.exception.DuplicateResourceException;
import com.ricklee.community.exception.ResourceNotFoundException;
import com.ricklee.community.exception.UnauthorizedException;
import com.ricklee.community.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // SecretKey 객체로 변환된 JWT 시크릿 키
    private SecretKey jwtSecretKey;

    /**
     * 생성자 주입을 통한 의존성 주입
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 애플리케이션 시작 시 JWT 시크릿 키를 SecretKey 객체로 변환
     */
    @PostConstruct
    public void init() {
        // 문자열 형태의 시크릿 키를 SecretKey 객체로 변환
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 회원가입 처리
     * @param requestDto 회원가입 요청 정보
     * @return 생성된 사용자의 ID
     * @throws DuplicateResourceException 이메일 또는 닉네임이 이미 존재하는 경우
     */
    @Transactional
    public Long signup(SignupRequestDto requestDto) {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new DuplicateResourceException("user", "email", requestDto.getEmail());
        }

        // 닉네임 중복 검사
        if (userRepository.existsByNickname(requestDto.getNickname())) {
            throw new DuplicateResourceException("user", "nickname", requestDto.getNickname());
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // User 엔티티 생성 및 저장
        User user = User.builder()
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .nickname(requestDto.getNickname())
                .profileImg(requestDto.getProfileImg())
                .build();

        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }

    /**
     * 로그인 처리
     * @param requestDto 로그인 요청 정보
     * @return 인증 토큰과 사용자 ID
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     * @throws UnauthorizedException 비밀번호가 일치하지 않는 경우
     */
    @Transactional(readOnly = true)
    public Map<String, Object> login(LoginRequestDto requestDto) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("user", "email", requestDto.getEmail()));

        // 비밀번호 검증
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("비밀번호가 일치하지 않습니다.");
        }

        // JWT 토큰 생성
        String token = generateToken(user.getId());

        // 응답 데이터 생성
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user_id", user.getId());

        return response;
    }

    /**
     * 회원 정보 수정
     * @param userId 대상 사용자 ID
     * @param requestDto 수정 요청 정보
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     * @throws DuplicateResourceException 닉네임이 이미 존재하는 경우
     */
    @Transactional
    public void updateUserInfo(Long userId, UserUpdateRequestDto requestDto) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user", "id", userId));

        // 닉네임이 변경된 경우에만 중복 검사
        if (!user.getNickname().equals(requestDto.getNickname()) &&
                userRepository.existsByNickname(requestDto.getNickname())) {
            throw new DuplicateResourceException("user", "nickname", requestDto.getNickname());
        }

        // 회원 정보 업데이트
        user.updateProfile(requestDto.getNickname(), requestDto.getProfileImg());
        userRepository.save(user);
    }

    /**
     * 비밀번호 변경
     * @param userId 대상 사용자 ID
     * @param requestDto 비밀번호 변경 요청 정보
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     * @throws UnauthorizedException 현재 비밀번호가 일치하지 않는 경우
     */
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequestDto requestDto) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user", "id", userId));

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 암호화 및 업데이트
        String encodedNewPassword = passwordEncoder.encode(requestDto.getNewPassword());
        user.updatePassword(encodedNewPassword);
        userRepository.save(user);
    }

    /**
     * 회원 탈퇴
     * @param userId 대상 사용자 ID
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional
    public void deleteUser(Long userId) {
        // 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("user", "id", userId);
        }

        // 사용자 삭제 (관련 데이터는 cascade 옵션에 따라 처리됨)
        userRepository.deleteById(userId);
    }

    /**
     * JWT 토큰 생성
     * @param userId 사용자 ID
     * @return 생성된 JWT 토큰
     */
    private String generateToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecretKey)  // 생성된 안전한 키 사용
                .compact();
    }

    /**
     * 토큰에서 사용자 ID 추출
     * @param token JWT 토큰
     * @return 사용자 ID
     * @throws UnauthorizedException 토큰이 유효하지 않은 경우
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtSecretKey)  // 생성된 안전한 키 사용
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        }
    }

    /**
     * 사용자 조회
     * @param userId 사용자 ID
     * @return 사용자 엔티티
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user", "id", userId));
    }
}