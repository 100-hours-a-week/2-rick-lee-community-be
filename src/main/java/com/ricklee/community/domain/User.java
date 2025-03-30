package com.ricklee.community.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    // 새로 추가된 S3 이미지 URL 필드
    @Column(name = "profile_img_url")
    private String profileImgUrl;

    // 양방향 관계 설정 (기존 코드)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    /**
     * 사용자 생성을 위한 빌더 패턴
     * @param email 사용자 이메일 (로그인 ID)
     * @param password 암호화된 비밀번호
     * @param nickname 사용자 닉네임
     * @param profileImgUrl 프로필 이미지 데이터 (선택 사항)
     */
    @Builder
    public User(String email, String password, String nickname, String profileImgUrl) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
    }

    /**
     * 회원 정보 수정 메서드 (프로필 이미지 URL 업데이트)
     */
    public void updateProfile(String nickname, String profileImgUrl) {
        this.nickname = nickname;
        if (profileImgUrl != null) {
            this.profileImgUrl = profileImgUrl;
        }
    }

    /**
     * 비밀번호 변경 메서드
     * @param newPassword 새로운 암호화된 비밀번호
     */
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    /**
     * 게시글 추가 편의 메서드
     * @param post 추가할 게시글
     */
    public void addPost(Post post) {
        this.posts.add(post);
        post.setUser(this);
    }

    /**
     * 댓글 추가 편의 메서드
     * @param comment 추가할 댓글
     */
    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setUser(this);
    }

    /**
     * 좋아요 추가 편의 메서드
     * @param like 추가할 좋아요
     */
    public void addLike(Like like) {
        this.likes.add(like);
        like.setUser(this);
    }
}