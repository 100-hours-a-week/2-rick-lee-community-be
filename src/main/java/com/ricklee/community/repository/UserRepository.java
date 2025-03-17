package com.ricklee.community.repository;

import com.ricklee.community.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 Repository 인터페이스
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회
     * @param email 이메일
     * @return 사용자 (Optional)
     */
    Optional<User> findByEmail(String email);

    /**
     * 닉네임으로 사용자 조회
     * @param nickname 닉네임
     * @return 사용자 (Optional)
     */
    Optional<User> findByNickname(String nickname);

    /**
     * 이메일이 존재하는지 확인
     * @param email 이메일
     * @return 존재 여부
     */
    boolean existsByEmail(String email);

    /**
     * 닉네임이 존재하는지 확인
     * @param nickname 닉네임
     * @return 존재 여부
     */
    boolean existsByNickname(String nickname);
}