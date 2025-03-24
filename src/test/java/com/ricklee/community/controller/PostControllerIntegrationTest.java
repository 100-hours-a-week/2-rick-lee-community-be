package com.ricklee.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ricklee.community.dto.user.LoginRequestDto;
import com.ricklee.community.dto.post.PostRequestDto;
import com.ricklee.community.dto.user.SignupRequestDto;
import com.ricklee.community.repository.CommentRepository;
import com.ricklee.community.repository.LikeRepository;
import com.ricklee.community.repository.PostRepository;
import com.ricklee.community.repository.UserRepository;
import com.ricklee.community.service.PostService;
import com.ricklee.community.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    private Long userId1;
    private Long userId2;
    private Long postId;
    private String token1;
    private String token2;

    @BeforeEach
    void setUp() throws Exception {
        // 기존 데이터 정리
        likeRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트 사용자 1 생성 및 로그인
        SignupRequestDto signupDto1 = new SignupRequestDto();
        signupDto1.setEmail("test1@example.com");
        signupDto1.setPassword("Test1234!");
        signupDto1.setNickname("testuser1");
        userId1 = userService.signup(signupDto1);

        LoginRequestDto loginDto1 = new LoginRequestDto();
        loginDto1.setEmail("test1@example.com");
        loginDto1.setPassword("Test1234!");
        Map<String, Object> loginResult1 = userService.login(loginDto1);
        token1 = (String) loginResult1.get("token");

        // 테스트 사용자 2 생성 및 로그인
        SignupRequestDto signupDto2 = new SignupRequestDto();
        signupDto2.setEmail("test2@example.com");
        signupDto2.setPassword("Test1234!");
        signupDto2.setNickname("testuser2");
        userId2 = userService.signup(signupDto2);

        LoginRequestDto loginDto2 = new LoginRequestDto();
        loginDto2.setEmail("test2@example.com");
        loginDto2.setPassword("Test1234!");
        Map<String, Object> loginResult2 = userService.login(loginDto2);
        token2 = (String) loginResult2.get("token");

        // 테스트 게시글 생성
        PostRequestDto postRequestDto = new PostRequestDto();
        postRequestDto.setTitle("테스트 제목");
        postRequestDto.setContent("테스트 내용");
        postId = postService.createPost(userId1, postRequestDto);
    }

    @AfterEach
    void tearDown() {
        likeRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("게시글 작성 API 통합 테스트 - 성공")
    void createPostSuccess() throws Exception {
        // given
        PostRequestDto requestDto = new PostRequestDto();
        requestDto.setTitle("새 게시글 제목");
        requestDto.setContent("새 게시글 내용");

        // when & then
        mockMvc.perform(post("/posts")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("post_created")))
                .andExpect(jsonPath("$.data.post_id", notNullValue()));
    }

    @Test
    @DisplayName("게시글 작성 API 통합 테스트 - 인증 실패")
    void createPostUnauthorized() throws Exception {
        // given
        PostRequestDto requestDto = new PostRequestDto();
        requestDto.setTitle("테스트 제목");
        requestDto.setContent("테스트 내용");

        // when & then
        mockMvc.perform(post("/posts")
                        .header("Authorization", "Bearer invalid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("게시글 작성 API 통합 테스트 - 빈 제목")
    void createPostWithEmptyTitle() throws Exception {
        // given
        PostRequestDto requestDto = new PostRequestDto();
        requestDto.setTitle("");  // 빈 제목
        requestDto.setContent("테스트 내용");

        // when & then
        mockMvc.perform(post("/posts")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 작성 API 통합 테스트 - 제목 길이 초과")
    void createPostWithTooLongTitle() throws Exception {
        // given
        PostRequestDto requestDto = new PostRequestDto();
        requestDto.setTitle("이 제목은 26자를 초과하는 매우 긴 제목입니다. 이렇게 길면 안됩니다.");  // 26자 초과
        requestDto.setContent("테스트 내용");

        // when & then
        mockMvc.perform(post("/posts")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 조회 API 통합 테스트 - 성공")
    void getPostSuccess() throws Exception {
        // when & then
        mockMvc.perform(get("/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + token1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("post_fetched")))
                .andExpect(jsonPath("$.data.postId", is(postId.intValue())))
                .andExpect(jsonPath("$.data.title", is("테스트 제목")))
                .andExpect(jsonPath("$.data.content", is("테스트 내용")));
    }

    @Test
    @DisplayName("게시글 조회 API 통합 테스트 - 게시글 없음")
    void getPostNotFound() throws Exception {
        // when & then
        mockMvc.perform(get("/posts/{postId}", 999)
                        .header("Authorization", "Bearer " + token1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("post_not_found")));
    }

    @Test
    @DisplayName("게시글 목록 조회 API 통합 테스트")
    void getPostListSuccess() throws Exception {
        // given
        // 추가 게시글 생성
        for (int i = 0; i < 5; i++) {
            PostRequestDto dto = new PostRequestDto();
            dto.setTitle("추가 게시글 " + i);
            dto.setContent("추가 내용 " + i);
            postService.createPost(userId1, dto);
        }

        // when & then
        mockMvc.perform(get("/posts")
                        .header("Authorization", "Bearer " + token1)
                        .param("page", "1")
                        .param("per_page", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("posts_list_retrieved")))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(greaterThanOrEqualTo(6))) // 최소 6개 (기존 1개 + 추가 5개)
                .andExpect(jsonPath("$.pagination").exists())
                .andExpect(jsonPath("$.pagination.page", is(1)))
                .andExpect(jsonPath("$.pagination.per_page", is(10)));
    }

    @Test
    @DisplayName("게시글 수정 API 통합 테스트 - 성공")
    void updatePostSuccess() throws Exception {
        // given
        PostRequestDto requestDto = new PostRequestDto();
        requestDto.setTitle("수정된 제목");
        requestDto.setContent("수정된 내용");

        // when & then
        mockMvc.perform(put("/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("post_updated")))
                .andExpect(jsonPath("$.data.post_id", is(postId.intValue())))
                .andExpect(jsonPath("$.data.title", is("수정된 제목")))
                .andExpect(jsonPath("$.data.content", is("수정된 내용")));
    }

    @Test
    @DisplayName("게시글 수정 API 통합 테스트 - 권한 없음")
    void updatePostForbidden() throws Exception {
        // given
        PostRequestDto requestDto = new PostRequestDto();
        requestDto.setTitle("수정 시도");
        requestDto.setContent("수정 내용");

        // when & then
        mockMvc.perform(put("/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + token2) // 다른 사용자의 토큰
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("unauthorized")));
    }

    @Test
    @DisplayName("게시글 삭제 API 통합 테스트 - 성공")
    void deletePostSuccess() throws Exception {
        // when & then
        mockMvc.perform(delete("/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + token1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("post_deleted")));
    }

    @Test
    @DisplayName("게시글 삭제 API 통합 테스트 - 권한 없음")
    void deletePostForbidden() throws Exception {
        // when & then
        mockMvc.perform(delete("/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + token2)) // 다른 사용자의 토큰
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("unauthorized")));
    }

    @Test
    @DisplayName("게시글 좋아요 추가 API 통합 테스트")
    void addLikeSuccess() throws Exception {
        // when & then
        mockMvc.perform(post("/posts/{postId}/like", postId)
                        .header("Authorization", "Bearer " + token2))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("like_created")))
                .andExpect(jsonPath("$.data.id", notNullValue()));
    }

    @Test
    @DisplayName("게시글 좋아요 중복 추가 API 통합 테스트")
    void addLikeDuplicate() throws Exception {
        // given
        // 먼저 좋아요 추가
        mockMvc.perform(post("/posts/{postId}/like", postId)
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isCreated());

        // 같은 사용자가 다시 좋아요 추가 시도
        mockMvc.perform(post("/posts/{postId}/like", postId)
                        .header("Authorization", "Bearer " + token2))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("invalid_request")));
    }

    @Test
    @DisplayName("게시글 좋아요 취소 API 통합 테스트")
    void removeLikeSuccess() throws Exception {
        // given
        // 먼저 좋아요 추가
        mockMvc.perform(post("/posts/{postId}/like", postId)
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isCreated());

        // when & then
        mockMvc.perform(delete("/posts/{postId}/like", postId)
                        .header("Authorization", "Bearer " + token2))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("like_removed")));
    }

    @Test
    @DisplayName("게시글 좋아요 취소 API 통합 테스트 - 좋아요 없음")
    void removeLikeNotFound() throws Exception {
        // when & then
        mockMvc.perform(delete("/posts/{postId}/like", postId)
                        .header("Authorization", "Bearer " + token2))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("post_not_found")));
    }
}