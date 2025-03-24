package com.ricklee.community.service;

import com.ricklee.community.domain.Post;
import com.ricklee.community.domain.User;
import com.ricklee.community.dto.post.PostDetailResponseDto;
import com.ricklee.community.dto.post.PostListItemDto;
import com.ricklee.community.dto.post.PostRequestDto;
import com.ricklee.community.exception.custom.ResourceNotFoundException;
import com.ricklee.community.exception.custom.UnauthorizedException;
import com.ricklee.community.repository.CommentRepository;
import com.ricklee.community.repository.LikeRepository;
import com.ricklee.community.repository.PostRepository;
import com.ricklee.community.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PostServiceIntegrationTest {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User testUser1;
    private User testUser2;
    private Post testPost;
    private Long userId1;
    private Long userId2;
    private Long postId;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser1 = User.builder()
                .email("test1@example.com")
                .password("password1")
                .nickname("testuser1")
                .build();
        userId1 = userRepository.save(testUser1).getId();

        testUser2 = User.builder()
                .email("test2@example.com")
                .password("password2")
                .nickname("testuser2")
                .build();
        userId2 = userRepository.save(testUser2).getId();

        // 테스트 게시글 생성
        PostRequestDto postRequestDto = new PostRequestDto();
        postRequestDto.setTitle("테스트 제목");
        postRequestDto.setContent("테스트 내용");
        postId = postService.createPost(userId1, postRequestDto);

        testPost = postRepository.findById(postId).orElseThrow();
    }

    @AfterEach
    void tearDown() {
        likeRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("게시글 작성 통합 테스트")
    void createPostIntegrationTest() {
        // given
        PostRequestDto postRequestDto = new PostRequestDto();
        postRequestDto.setTitle("새 게시글 제목");
        postRequestDto.setContent("새 게시글 내용");

        // when
        Long newPostId = postService.createPost(userId1, postRequestDto);

        // then
        Post savedPost = postRepository.findById(newPostId).orElse(null);
        assertNotNull(savedPost);
        assertEquals("새 게시글 제목", savedPost.getTitle());
        assertEquals("새 게시글 내용", savedPost.getContent());
        assertEquals(userId1, savedPost.getUser().getId());
    }

    @Test
    @DisplayName("게시글 상세 조회 통합 테스트")
    void getPostDetailIntegrationTest() {
        // when
        PostDetailResponseDto postDetail = postService.getPostDetail(postId, userId1);

        // then
        assertNotNull(postDetail);
        assertEquals(postId, postDetail.getPostId());
        assertEquals("테스트 제목", postDetail.getTitle());
        assertEquals("테스트 내용", postDetail.getContent());
        assertEquals(1, postDetail.getViewCount()); // 조회수 증가 확인
    }

    @Test
    @DisplayName("게시글 목록 조회 통합 테스트")
    void getPostListIntegrationTest() {
        // given
        // 추가 게시글 생성
        for (int i = 0; i < 5; i++) {
            PostRequestDto dto = new PostRequestDto();
            dto.setTitle("추가 게시글 " + i);
            dto.setContent("추가 내용 " + i);
            postService.createPost(userId1, dto);
        }

        // when
        List<PostListItemDto> posts = postService.getPostList(0, 10);
        Map<String, Object> pagination = postService.getPaginationInfo(1, 10);

        // then
        assertNotNull(posts);
        assertTrue(posts.size() >= 6); // 기존 1개 + 추가 5개
        assertEquals(1, pagination.get("page"));
        assertEquals(10, pagination.get("per_page"));
        assertTrue((long)pagination.get("total_items") >= 6);
    }

    @Test
    @DisplayName("게시글 수정 통합 테스트")
    void updatePostIntegrationTest() {
        // given
        PostRequestDto updateDto = new PostRequestDto();
        updateDto.setTitle("수정된 제목");
        updateDto.setContent("수정된 내용");

        // when
        Map<String, Object> result = postService.updatePost(userId1, postId, updateDto);

        // then
        Post updatedPost = postRepository.findById(postId).orElse(null);
        assertNotNull(updatedPost);
        assertEquals("수정된 제목", updatedPost.getTitle());
        assertEquals("수정된 내용", updatedPost.getContent());
        assertEquals(postId, result.get("post_id"));
    }

    @Test
    @DisplayName("게시글 수정 권한 없음 통합 테스트")
    void updatePostUnauthorizedIntegrationTest() {
        // given
        PostRequestDto updateDto = new PostRequestDto();
        updateDto.setTitle("수정 시도");
        updateDto.setContent("수정 내용");

        // when & then
        assertThrows(UnauthorizedException.class, () -> {
            postService.updatePost(userId2, postId, updateDto); // 다른 사용자가 수정 시도
        });
    }

    @Test
    @DisplayName("게시글 삭제 통합 테스트")
    void deletePostIntegrationTest() {
        // when
        postService.deletePost(userId1, postId);

        // then
        assertFalse(postRepository.existsById(postId));
    }

    @Test
    @DisplayName("게시글 삭제 권한 없음 통합 테스트")
    void deletePostUnauthorizedIntegrationTest() {
        // when & then
        assertThrows(UnauthorizedException.class, () -> {
            postService.deletePost(userId2, postId); // 다른 사용자가 삭제 시도
        });
    }

    @Test
    @DisplayName("게시글 좋아요 추가 통합 테스트")
    void addLikeIntegrationTest() {
        // when
        Long likeCount = postService.addLike(userId2, postId);

        // then
        assertTrue(likeCount > 0);
        assertTrue(likeService.hasUserLikedPost(userId2, postId));
    }

    @Test
    @DisplayName("게시글 좋아요 취소 통합 테스트")
    void removeLikeIntegrationTest() {
        // given
        postService.addLike(userId2, postId);

        // when
        Long likeCount = postService.removeLike(userId2, postId);

        // then
        assertFalse(likeService.hasUserLikedPost(userId2, postId));
    }

    @Test
    @DisplayName("게시글 좋아요 수 조회 통합 테스트")
    void countLikesIntegrationTest() {
        // given
        postService.addLike(userId1, postId);
        postService.addLike(userId2, postId);

        // when
        Long likeCount = likeRepository.countByPostId(postId);

        // then
        assertEquals(2L, likeCount);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 테스트")
    void getNonExistingPostTest() {
        // when & then
        assertThrows(ResourceNotFoundException.class, () -> {
            postService.getPostDetail(9999L, userId1);
        });
    }
}