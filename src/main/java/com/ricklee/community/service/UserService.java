package com.ricklee.community.service;

import com.ricklee.community.domain.User;
import com.ricklee.community.dto.user.*;
import com.ricklee.community.exception.custom.DuplicateResourceException;
import com.ricklee.community.exception.custom.ResourceNotFoundException;
import com.ricklee.community.exception.custom.UnauthorizedException;
import com.ricklee.community.repository.UserRepository;
import com.ricklee.community.util.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ImageService imageService;

    /**
     * 사용자 정보 조회
     * @param userId 사용자 ID
     * @return 사용자 정보 DTO
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserInfo(Long userId) {
        User user = getUserById(userId);

        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImgUrl(user.getProfileImgUrl())
                .build();
    }

    /**
     * 회원가입 처리
     * @param requestDto 회원가입 요청 정보
     * @param profileImage 프로필 이미지 파일 (선택사항)
     * @return 생성된 사용자의 ID
     * @throws DuplicateResourceException 이메일 또는 닉네임이 이미 존재하는 경우
     */
    @Transactional
    public Long signup(SignupRequestDto requestDto, MultipartFile profileImage) {
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

        // 이미지 업로드
        String profileImgUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            profileImgUrl = imageService.uploadFile(profileImage, "profiles");
        }

        // User 엔티티 생성 및 저장
        User user = User.builder()
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .nickname(requestDto.getNickname())
                .profileImgUrl(profileImgUrl)
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

        // JWT 토큰 생성 - JwtUtil 사용
        String token = jwtUtil.generateToken(user.getId(), "MEMBER");

        // 응답 데이터 생성
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user_id", user.getId());

        return response;
    }

    /**
     * 닉네임 사용 가능 여부 확인
     * @param nickname 확인할 닉네임
     * @return 사용 가능하면 true, 중복이면 false
     */
    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    /**
     * 프로필 이미지 업로드
     * @param userId 사용자 ID
     * @param file 프로필 이미지 파일
     * @return 업로드된 이미지 URL
     */
    @Transactional
    public String uploadProfileImage(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어 있습니다.");
        }

        User user = getUserById(userId);

        // 기존 이미지가 있으면 S3에서 삭제
        if (user.getProfileImgUrl() != null && !user.getProfileImgUrl().isEmpty()) {
            imageService.deleteFile(user.getProfileImgUrl());
        }

        // S3에 이미지 업로드
        String imageUrl = imageService.uploadFile(file, "profiles");

        // 사용자 프로필 이미지 URL 업데이트
        user.updateProfile(user.getNickname(), imageUrl);
        userRepository.save(user);

        return imageUrl;
    }

    /**
     * 회원 정보 수정
     * @param userId 대상 사용자 ID
     * @param nickname 새 닉네임
     * @param file 프로필 이미지 파일 (선택사항)
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     * @throws DuplicateResourceException 닉네임이 이미 존재하는 경우
     */
    @Transactional
    public void updateUserInfo(Long userId, String nickname, MultipartFile file) {
        // 사용자 조회
        User user = getUserById(userId);

        // 닉네임이 변경된 경우에만 중복 검사
        if (!user.getNickname().equals(nickname) &&
                userRepository.existsByNickname(nickname)) {
            throw new DuplicateResourceException("user", "nickname", nickname);
        }

        // 이미지 처리
        String profileImgUrl = user.getProfileImgUrl();
        if (file != null && !file.isEmpty()) {
            // 기존 이미지가 있으면 S3에서 삭제
            if (profileImgUrl != null && !profileImgUrl.isEmpty()) {
                imageService.deleteFile(profileImgUrl);
            }

            // 새 이미지 업로드
            profileImgUrl = imageService.uploadFile(file, "profiles");
        }

        // 회원 정보 업데이트
        user.updateProfile(nickname, profileImgUrl);
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
        User user = getUserById(userId);

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
        User user = getUserById(userId);

        // 프로필 이미지가 있다면 S3에서 삭제
        if (user.getProfileImgUrl() != null && !user.getProfileImgUrl().isEmpty()) {
            imageService.deleteFile(user.getProfileImgUrl());
        }

        // 사용자 삭제 (관련 데이터는 cascade 옵션에 따라 처리됨)
        userRepository.deleteById(userId);
    }

    /**
     * 토큰에서 사용자 ID 추출 - JwtUtil 사용
     * @param token JWT 토큰
     * @return 사용자 ID
     * @throws UnauthorizedException 토큰이 유효하지 않은 경우
     */
    public Long getUserIdFromToken(String token) {
        try {
            return jwtUtil.getUserIdFromToken(token);
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