package com.ricklee.community.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    /**
     * 좋아요 생성을 위한 빌더 패턴
     * @param user 좋아요를 누른 사용자
     * @param post 좋아요 대상 게시글
     */
    @Builder
    private Like(User user, Post post) {
        this.user = user;
        this.post = post;
    }

    /**
     * 좋아요와 사용자 연관관계 설정
     * @param user 좋아요를 누른 사용자
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * 좋아요와 게시글 연관관계 설정
     * @param post 좋아요 대상 게시글
     */
    public void setPost(Post post) {
        this.post = post;
    }

    /**
     * 특정 사용자가 특정 게시글에 좋아요를 눌렀을 때 사용하는 정적 팩토리 메서드
     * @param user 사용자
     * @param post 게시글
     * @return 새로운 Like 엔티티
     */
    public static Like createLike(User user, Post post) {
        return Like.builder()
                .user(user)
                .post(post)
                .build();
    }
}