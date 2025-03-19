package com.ricklee.community.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * Like 엔티티의 복합 키 클래스
 * user_id와 post_id를 조합하여 복합 키로 사용
 */
public class LikeId implements Serializable {

    private Long user;  // user_id에 매핑
    private Long post;  // post_id에 매핑

    // JPA 요구사항: 기본 생성자
    public LikeId() {
    }

    public LikeId(Long user, Long post) {
        this.user = user;
        this.post = post;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LikeId likeId = (LikeId) o;
        return Objects.equals(user, likeId.user) &&
                Objects.equals(post, likeId.post);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, post);
    }
}