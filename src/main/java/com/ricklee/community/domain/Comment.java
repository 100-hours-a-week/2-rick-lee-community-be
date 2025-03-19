package com.ricklee.community.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 댓글 정보를 담는 엔티티
 */
@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    /**
     * 댓글 생성을 위한 빌더 패턴
     * @param content 댓글 내용
     */
    @Builder
    public Comment(String content) {
        this.content = content;
    }

    /**
     * 댓글과 사용자 연관관계 설정
     * @param user 댓글 작성자
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * 댓글과 게시글 연관관계 설정
     * @param post 연관 게시글
     */
    public void setPost(Post post) {
        this.post = post;
    }

    /**
     * 댓글 내용 수정 메서드
     * @param content 수정할 내용
     */
    public void updateContent(String content) {
        this.content = content;
    }
}