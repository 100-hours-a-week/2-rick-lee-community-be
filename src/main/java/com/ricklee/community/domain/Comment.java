package com.ricklee.community.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> childComments = new ArrayList<>();

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
     * 부모 댓글 설정 (대댓글인 경우)
     * @param parentComment 부모 댓글
     */
    public void setParentComment(Comment parentComment) {
        this.parentComment = parentComment;
        parentComment.getChildComments().add(this);
    }

    /**
     * 댓글 내용 수정 메서드
     * @param content 수정할 내용
     */
    public void updateContent(String content) {
        this.content = content;
    }

    /**
     * 대댓글 추가 편의 메서드
     * @param childComment 추가할 대댓글
     */
    public void addChildComment(Comment childComment) {
        this.childComments.add(childComment);
        childComment.setParentComment(this);
    }

    /**
     * 댓글이 대댓글인지 확인하는 메서드
     * @return 대댓글 여부
     */
    public boolean isChildComment() {
        return this.parentComment != null;
    }
}