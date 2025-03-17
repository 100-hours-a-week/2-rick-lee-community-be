package com.ricklee.community.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    /**
     * 게시글 생성을 위한 빌더 패턴
     * @param title 게시글 제목
     * @param content 게시글 내용
     * @param imageUrl 게시글 이미지 URL (선택 사항)
     */
    @Builder
    public Post(String title, String content, String imageUrl) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.viewCount = 0;
    }

    /**
     * 게시글과 사용자 연관관계 설정
     * @param user 게시글 작성자
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * 게시글 수정 메서드
     * @param title 수정할 제목
     * @param content 수정할 내용
     * @param imageUrl 수정할 이미지 URL
     */
    public void update(String title, String content, String imageUrl) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    /**
     * 조회수 증가 메서드
     */
    public void increaseViewCount() {
        this.viewCount++;
    }

    /**
     * 댓글 추가 편의 메서드
     * @param comment 추가할 댓글
     */
    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setPost(this);
    }

    /**
     * 좋아요 추가 편의 메서드
     * @param like 추가할 좋아요
     */
    public void addLike(Like like) {
        this.likes.add(like);
        like.setPost(this);
    }

    /**
     * 댓글 수 조회 메서드
     * @return 댓글 수
     */
    public int getCommentCount() {
        return this.comments.size();
    }

    /**
     * 좋아요 수 조회 메서드
     * @return 좋아요 수
     */
    public int getLikeCount() {
        return this.likes.size();
    }
}