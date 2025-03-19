package com.ricklee.community.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 게시글 좋아요 정보를 담는 엔티티
 * user_id와 post_id의 복합키를 사용하여 사용자 당 게시글 하나에 하나의 좋아요만 가능하도록 제약
 */
@Entity
@Table(name = "likes")
@IdClass(LikeId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Like {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * 엔티티 생성을 위한 protected 생성자
     * 직접 생성보다는 정적 팩토리 메서드 사용 권장
     */
    protected Like(User user, Post post) {
        this.user = user;
        this.post = post;
    }

    /**
     * 좋아요 생성을 위한 정적 팩토리 메서드
     * @param user 좋아요를 누른 사용자
     * @param post 좋아요 대상 게시글
     * @return 생성된 Like 엔티티
     */
    public static Like createLike(User user, Post post) {
        Like like = new Like(user, post);
        return like;
    }

    /**
     * 사용자 설정 (양방향 관계 설정용)
     * @param user 좋아요를 누른 사용자
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * 게시글 설정 (양방향 관계 설정용)
     * @param post 좋아요 대상 게시글
     */
    public void setPost(Post post) {
        this.post = post;
    }
}