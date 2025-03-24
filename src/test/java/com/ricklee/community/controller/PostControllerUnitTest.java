package com.ricklee.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ricklee.community.dto.PostDetailResponseDto;
import com.ricklee.community.dto.PostListItemDto;
import com.ricklee.community.dto.PostRequestDto;
import com.ricklee.community.exception.ResourceNotFoundException;
import com.ricklee.community.exception.UnauthorizedException;
import com.ricklee.community.service.PostService;
import com.ricklee.community.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class PostControllerUnitTest {

    @Mock
    private PostService postService;

    @Mock
    private UserService userService;

    @InjectMocks
    private PostController postController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private final String VALID_TOKEN = "Bearer valid_token";
    private final Long VALID_USER_ID = 1L;
    private final Long VALID_POST_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(postController).build();
        objectMapper = new ObjectMapper();

        // 기본 토큰 검증 설정
        lenient().when(userService.getUserIdFromToken(VALID_TOKEN.replace("Bearer ", "")))
                .thenReturn(VALID_USER_ID);
    }

    @Test
    @DisplayName("게시글 작성 API 테스트 - 성공")
    void createPostSuccess() throws Exception {
        // given
        PostRequestDto requestDto = new PostRequestDto();
        requestDto.setTitle("테스트 제목");
        requestDto.setContent("테스트 내용");

        given(postService.createPost(eq(VALID_USER_ID), any()))
                .willReturn(VALID_POST_ID);

        // when & then
        mockMvc.perform(post("/posts")
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("post_created")))
                .andExpect(jsonPath("$.data.post_id", is(VALID_POST_ID.intValue())));
    }

    @Test
    @DisplayName("게시글 작성 API 테스트 - 인증 실패")
    void createPostUnauthorized() throws Exception {
        // given
        PostRequestDto requestDto = new PostRequestDto();
        requestDto.setTitle("테스트 제목");
        requestDto.setContent("테스트 내용");

        when(userService.getUserIdFromToken("invalid_token"))
                .thenThrow(new UnauthorizedException("유효하지 않은 토큰입니다."));

        // when & then
        mockMvc.perform(post("/posts")
                        .header("Authorization", "Bearer invalid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("unauthorized")));
    }

    @Test
    @DisplayName("게시글 조회 API 테스트 - 성공")
    void getPostSuccess() throws Exception {
        // given
        PostDetailResponseDto responseDto = mock(PostDetailResponseDto.class);

        given(postService.getPostDetail(eq(VALID_POST_ID), eq(VALID_USER_ID)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/posts/{postId}", VALID_POST_ID)
                        .header("Authorization", VALID_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("post_fetched")))
                .andExpect(jsonPath("$.data", notNullValue()));
    }

    @Test
    @DisplayName("게시글 조회 API 테스트 - 게시글 없음")
    void getPostNotFound() throws Exception {
        // given
        given(postService.getPostDetail(eq(999L), eq(VALID_USER_ID)))
                .willThrow(new ResourceNotFoundException("post", "id", 999L));

        // when & then
        mockMvc.perform(get("/posts/{postId}", 999L)
                        .header("Authorization", VALID_TOKEN))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("post_not_found")));
    }

    @Test
    @DisplayName("게시글 수정 API 테스트 - 성공")
    void updatePostSuccess() throws Exception {
        // given
        PostRequestDto requestDto = new PostRequestDto();
        requestDto.setTitle("수정된 제목");
        requestDto.setContent("수정된 내용");

        Map<String, Object> updatedPost = new HashMap<>();
        updatedPost.put("post_id", VALID_POST_ID);
        updatedPost.put("title", "수정된 제목");
        updatedPost.put("content", "수정된 내용");

        given(postService.updatePost(eq(VALID_USER_ID), eq(VALID_POST_ID), any()))
                .willReturn(updatedPost);

        // when & then
        mockMvc.perform(put("/posts/{postId}", VALID_POST_ID)
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("post_updated")))
                .andExpect(jsonPath("$.data.post_id", is(VALID_POST_ID.intValue())))
                .andExpect(jsonPath("$.data.title", is("수정된 제목")));
    }

    @Test
    @DisplayName("게시글 수정 API 테스트 - 권한 없음")
    void updatePostForbidden() throws Exception {
        // given
        PostRequestDto requestDto = new PostRequestDto();
        requestDto.setTitle("수정된 제목");
        requestDto.setContent("수정된 내용");

        given(postService.updatePost(eq(VALID_USER_ID), eq(VALID_POST_ID), any()))
                .willThrow(new UnauthorizedException("게시글 수정 권한이 없습니다."));

        // when & then
        mockMvc.perform(put("/posts/{postId}", VALID_POST_ID)
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("unauthorized")));
    }

    @Test
    @DisplayName("게시글 삭제 API 테스트 - 성공")
    void deletePostSuccess() throws Exception {
        // given
        doNothing().when(postService).deletePost(VALID_USER_ID, VALID_POST_ID);

        // when & then
        mockMvc.perform(delete("/posts/{postId}", VALID_POST_ID)
                        .header("Authorization", VALID_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("post_deleted")));
    }

    @Test
    @DisplayName("게시글 목록 조회 API 테스트 - 성공")
    void getPostListSuccess() throws Exception {
        // given
        List<PostListItemDto> posts = new ArrayList<>();
        // 게시글 목록 데이터는 여기서 필요하지 않음

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total_count", 20);
        pagination.put("total_pages", 2);
        pagination.put("current_page", 1);

        given(postService.getPostList(eq(0), eq(10)))
                .willReturn(posts);

        given(postService.getPaginationInfo(eq(1), eq(10)))
                .willReturn(pagination);

        // when & then
        mockMvc.perform(get("/posts")
                        .header("Authorization", VALID_TOKEN)
                        .param("page", "1")
                        .param("per_page", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("posts_list_retrieved")))
                .andExpect(jsonPath("$.pagination.total_pages", is(2)))
                .andExpect(jsonPath("$.pagination.current_page", is(1)));
    }

    @Test
    @DisplayName("게시글 좋아요 추가 API 테스트 - 성공")
    void addLikeSuccess() throws Exception {
        // given
        Long likeId = 1L;
        given(postService.addLike(VALID_USER_ID, VALID_POST_ID))
                .willReturn(likeId);

        // when & then
        mockMvc.perform(post("/posts/{postId}/like", VALID_POST_ID)
                        .header("Authorization", VALID_TOKEN))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("like_created")))
                .andExpect(jsonPath("$.data.id", is(likeId.intValue())));
    }

    @Test
    @DisplayName("게시글 좋아요 추가 API 테스트 - 중복 좋아요")
    void addLikeDuplicate() throws Exception {
        // given
        given(postService.addLike(VALID_USER_ID, VALID_POST_ID))
                .willThrow(new IllegalArgumentException("duplicate_like"));

        // when & then
        mockMvc.perform(post("/posts/{postId}/like", VALID_POST_ID)
                        .header("Authorization", VALID_TOKEN))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("invalid_request")));
    }

    @Test
    @DisplayName("게시글 좋아요 취소 API 테스트 - 성공")
    void removeLikeSuccess() throws Exception {
        // given
        Long removedLikeId = 1L;
        given(postService.removeLike(VALID_USER_ID, VALID_POST_ID))
                .willReturn(removedLikeId);

        // when & then
        mockMvc.perform(delete("/posts/{postId}/like", VALID_POST_ID)
                        .header("Authorization", VALID_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("like_removed")))
                .andExpect(jsonPath("$.data.id", is(removedLikeId.intValue())));
    }

    @Test
    @DisplayName("게시글 좋아요 취소 API 테스트 - 좋아요 없음")
    void removeLikeNotFound() throws Exception {
        // given
        given(postService.removeLike(VALID_USER_ID, VALID_POST_ID))
                .willThrow(new IllegalArgumentException("like_not_found"));

        // when & then
        mockMvc.perform(delete("/posts/{postId}/like", VALID_POST_ID)
                        .header("Authorization", VALID_TOKEN))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("post_not_found")));
    }

    @Test
    @DisplayName("빈 제목으로 게시글 작성 API 테스트 - 실패")
    void createPostWithEmptyTitle() throws Exception {
        // given
        PostRequestDto requestDto = new PostRequestDto();
        requestDto.setTitle("");  // 빈 제목
        requestDto.setContent("테스트 내용");

        // when & then
        mockMvc.perform(post("/posts")
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("제목 길이 초과로 게시글 작성 API 테스트 - 실패")
    void createPostWithTooLongTitle() throws Exception {
        // given
        PostRequestDto requestDto = new PostRequestDto();
        requestDto.setTitle("이 제목은 26자를 초과하는 매우 긴 제목입니다. 이렇게 길면 안됩니다.");  // 26자 초과
        requestDto.setContent("테스트 내용");

        // when & then
        mockMvc.perform(post("/posts")
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}