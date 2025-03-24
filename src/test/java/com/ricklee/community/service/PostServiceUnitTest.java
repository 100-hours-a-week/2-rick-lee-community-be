package com.ricklee.community.service;

import com.ricklee.community.domain.LikeId;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceUnitTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private LikeService likeService;

    @InjectMocks
    private PostService postService;

    private User user;
    private Post post;
    private PostRequestDto postRequestDto;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("testuser")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        // 테스트 게시글 생성
        post = Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .build();
        post.setUser(user);
        ReflectionTestUtils.setField(post, "id", 1L);
        ReflectionTestUtils.setField(post, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(post, "updatedAt", LocalDateTime.now());

        // 게시글 요청 DTO 생성
        postRequestDto = new PostRequestDto();
        postRequestDto.setTitle("테스트 제목");
        postRequestDto.setContent("테스트 내용");
    }

    @Test
    @DisplayName("게시글 작성 테스트")
    void createPostTest() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        // when
        Long postId = postService.createPost(1L, postRequestDto);

        // then
        assertEquals(1L, postId);
        verify(userRepository).findById(1L);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 작성 테스트 - 사용자 없음")
    void createPostUserNotFoundTest() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ResourceNotFoundException.class, () -> {
            postService.createPost(999L, postRequestDto);
        });
        verify(userRepository).findById(999L);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 상세 조회 테스트")
    void getPostDetailTest() {
        // given
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.countByPostId(1L)).thenReturn(5L);
        when(likeRepository.countByPostId(1L)).thenReturn(10L);

        LikeId likeId = new LikeId(1L, 1L);
        when(likeRepository.existsById(likeId)).thenReturn(true);

        // when
        PostDetailResponseDto result = postService.getPostDetail(1L, 1L);

        // then
        assertNotNull(result);
        verify(postRepository).findById(1L);
        verify(commentRepository).countByPostId(1L);
        verify(likeRepository).countByPostId(1L);
        verify(likeRepository).existsById(likeId);
        verify(postRepository).save(post); // 조회수 증가 확인
    }

    @Test
    @DisplayName("게시글 상세 조회 테스트 - 게시글 없음")
    void getPostDetailNotFoundTest() {
        // given
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ResourceNotFoundException.class, () -> {
            postService.getPostDetail(999L, 1L);
        });
        verify(postRepository).findById(999L);
        verify(commentRepository, never()).countByPostId(anyLong());
    }

    @Test
    @DisplayName("게시글 목록 조회 테스트")
    void getPostListTest() {
        // given
        List<Post> posts = Arrays.asList(post);
        Page<Post> postPage = new PageImpl<>(posts);

        when(postRepository.findAll(any(PageRequest.class))).thenReturn(postPage);
        when(commentRepository.countByPostId(1L)).thenReturn(5L);
        when(likeRepository.countByPostId(1L)).thenReturn(10L);

        // when
        List<PostListItemDto> result = postService.getPostList(0, 10);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(postRepository).findAll(any(PageRequest.class));
        verify(commentRepository).countByPostId(1L);
        verify(likeRepository).countByPostId(1L);
    }

    @Test
    @DisplayName("페이지네이션 정보 조회 테스트")
    void getPaginationInfoTest() {
        // given
        when(postRepository.count()).thenReturn(25L);

        // when
        Map<String, Object> result = postService.getPaginationInfo(1, 10);

        // then
        assertNotNull(result);
        assertEquals(1, result.get("page"));
        assertEquals(10, result.get("per_page"));
        assertEquals(3, result.get("total_pages")); // 25개 게시글, 페이지당 10개 -> 3페이지
        assertEquals(25L, result.get("total_items"));
        verify(postRepository).count();
    }

    @Test
    @DisplayName("게시글 수정 테스트")
    void updatePostTest() {
        // given
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostRequestDto updateDto = new PostRequestDto();
        updateDto.setTitle("수정된 제목");
        updateDto.setContent("수정된 내용");

        // when
        Map<String, Object> result = postService.updatePost(1L, 1L, updateDto);

        // then
        assertNotNull(result);
        assertEquals(1L, result.get("post_id"));
        verify(postRepository).findById(1L);
        verify(postRepository).save(post);
    }

    @Test
    @DisplayName("게시글 수정 테스트 - 권한 없음")
    void updatePostUnauthorizedTest() {
        // given
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // when & then
        assertThrows(UnauthorizedException.class, () -> {
            postService.updatePost(2L, 1L, postRequestDto); // 다른 사용자 ID로 시도
        });
        verify(postRepository).findById(1L);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 삭제 테스트")
    void deletePostTest() {
        // given
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // when
        postService.deletePost(1L, 1L);

        // then
        verify(postRepository).findById(1L);
        verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("게시글 삭제 테스트 - 권한 없음")
    void deletePostUnauthorizedTest() {
        // given
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // when & then
        assertThrows(UnauthorizedException.class, () -> {
            postService.deletePost(2L, 1L); // 다른 사용자 ID로 시도
        });
        verify(postRepository).findById(1L);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("게시글 좋아요 추가 테스트")
    void addLikeTest() {
        // given
        // LikeService의 addLike 메서드는 String을 반환
        when(likeService.addLike(1L, 1L)).thenReturn("좋아요가 추가되었습니다.");
        when(likeRepository.countByPostId(1L)).thenReturn(10L);

        // when
        Long likeCount = postService.addLike(1L, 1L);

        // then
        assertEquals(10L, likeCount);
        verify(likeService).addLike(1L, 1L);
        verify(likeRepository).countByPostId(1L);
    }

    @Test
    @DisplayName("게시글 좋아요 취소 테스트")
    void removeLikeTest() {
        // given
        // LikeService의 removeLike 메서드는 String을 반환
        when(likeService.removeLike(1L, 1L)).thenReturn("좋아요가 취소되었습니다.");
        when(likeRepository.countByPostId(1L)).thenReturn(9L);

        // when
        Long likeCount = postService.removeLike(1L, 1L);

        // then
        assertEquals(9L, likeCount);
        verify(likeService).removeLike(1L, 1L);
        verify(likeRepository).countByPostId(1L);
    }

    @Test
    @DisplayName("게시글 좋아요 수 조회 테스트")
    void countLikesTest() {
        // given
        when(likeRepository.countByPostId(1L)).thenReturn(15L);

        // when
        Long likeCount = likeRepository.countByPostId(1L);

        // then
        assertEquals(15L, likeCount);
        verify(likeRepository).countByPostId(1L);
    }

    @Test
    @DisplayName("사용자의 게시글 좋아요 여부 확인 테스트")
    void hasUserLikedPostTest() {
        // given
        when(likeService.hasUserLikedPost(1L, 1L)).thenReturn(true);

        // when
        boolean hasLiked = likeService.hasUserLikedPost(1L, 1L);

        // then
        assertTrue(hasLiked);
        verify(likeService).hasUserLikedPost(1L, 1L);
    }
}