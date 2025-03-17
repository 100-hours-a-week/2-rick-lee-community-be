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

    @Column(name = "profile_image")
    private String profileImage;

    // 양방향 관계 설정 (필요에 따라 주석 해제하여 사용)
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
     * @param profileImage 프로필 이미지 URL (선택 사항)
     */
    @Builder
    public User(String email, String password, String nickname, String profileImage) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    /**
     * 회원 정보 수정 메서드
     * @param nickname 새로운 닉네임
     * @param profileImage 새로운 프로필 이미지 URL
     */
    public void updateProfile(String nickname, String profileImage) {
        this.nickname = nickname;
        this.profileImage = profileImage;
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