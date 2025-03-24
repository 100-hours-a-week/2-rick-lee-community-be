package com.ricklee.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ricklee.community.dto.comment.CommentRequestDto;
import com.ricklee.community.dto.user.LoginRequestDto;
import com.ricklee.community.dto.post.PostRequestDto;
import com.ricklee.community.dto.user.SignupRequestDto;
import com.ricklee.community.repository.CommentRepository;
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

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CommentControllerIntegrationTest {

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
    private CommentRepository commentRepository;

    private Long userId1;
    private Long userId2;
    private Long postId;
    private Long commentId;
    private String token1;
    private String token2;

    @BeforeEach
    void setUp() throws Exception {
        // 기존 데이터 정리
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트 사용자 1 생성 및 로그인 (게시글 작성자)
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

        // 테스트 사용자 2 생성 및 로그인 (댓글 작성자)
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
        postRequestDto.setTitle("테스트 게시글");
        postRequestDto.setContent("테스트 내용");
        postId = postService.createPost(userId1, postRequestDto);

        // 테스트 댓글 생성
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setPostId(postId);
        commentRequestDto.setContent("테스트 댓글");

        mockMvc.perform(post("/comments")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("comment_created")));

        // 생성된 댓글 ID 저장
        commentId = commentRepository.findAll().get(0).getId();
    }

    @AfterEach
    void tearDown() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("댓글 작성 API 통합 테스트 - 성공")
    void createCommentSuccess() throws Exception {
        // given
        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setPostId(postId);
        requestDto.setContent("새로운 테스트 댓글");

        // when & then
        mockMvc.perform(post("/comments")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("comment_created")))
                .andExpect(jsonPath("$.data.comment_id", notNullValue()));
    }

    @Test
    @DisplayName("댓글 작성 API 통합 테스트 - 게시글 없음")
    void createCommentPostNotFound() throws Exception {
        // given
        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setPostId(9999L); // 존재하지 않는 게시글 ID
        requestDto.setContent("테스트 댓글 내용");

        // when & then
        mockMvc.perform(post("/comments")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("post_not_found")));
    }

    @Test
    @DisplayName("댓글 작성 API 통합 테스트 - 인증 실패")
    void createCommentUnauthorized() throws Exception {
        // given
        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setPostId(postId);
        requestDto.setContent("테스트 댓글 내용");

        // when & then
        mockMvc.perform(post("/comments")
                        .header("Authorization", "Bearer invalid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("댓글 목록 조회 API 통합 테스트 - 성공")
    void getCommentsSuccess() throws Exception {
        // when & then
        mockMvc.perform(get("/posts/{postId}/comments", postId)
                        .header("Authorization", "Bearer " + token1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("comments_retrieved")))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].content", is("테스트 댓글")));
    }

    @Test
    @DisplayName("댓글 목록 조회 API 통합 테스트 - 게시글 없음")
    void getCommentsPostNotFound() throws Exception {
        // when & then
        mockMvc.perform(get("/posts/{postId}/comments", 9999L)
                        .header("Authorization", "Bearer " + token1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("post_not_found")));
    }

    @Test
    @DisplayName("댓글 수정 API 통합 테스트 - 성공")
    void updateCommentSuccess() throws Exception {
        // given
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("content", "수정된 댓글 내용");

        // when & then
        mockMvc.perform(put("/comments/{commentId}", commentId)
                        .header("Authorization", "Bearer " + token2) // 댓글 작성자만 수정 가능
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("comment_updated")))
                .andExpect(jsonPath("$.data.content", is("수정된 댓글 내용")));
    }

    @Test
    @DisplayName("댓글 수정 API 통합 테스트 - 권한 없음")
    void updateCommentUnauthorized() throws Exception {
        // given
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("content", "수정된 댓글 내용");

        // when & then
        mockMvc.perform(put("/comments/{commentId}", commentId)
                        .header("Authorization", "Bearer " + token1) // 게시글 작성자가 다른 사람의 댓글 수정 시도
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("댓글 수정 권한이 없습니다.")));
    }

    @Test
    @DisplayName("댓글 수정 API 통합 테스트 - 빈 내용")
    void updateCommentEmptyContent() throws Exception {
        // given
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("content", "");

        // when & then
        mockMvc.perform(put("/comments/{commentId}", commentId)
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("invalid_request")));
    }

    @Test
    @DisplayName("댓글 삭제 API 통합 테스트 - 성공")
    void deleteCommentSuccess() throws Exception {
        // when & then
        mockMvc.perform(delete("/comments/{commentId}", commentId)
                        .header("Authorization", "Bearer " + token2)) // 댓글 작성자만 삭제 가능
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("comment_deleted")))
                .andExpect(jsonPath("$.data.comment_id", is(commentId.intValue())));
    }

    @Test
    @DisplayName("댓글 삭제 API 통합 테스트 - 권한 없음")
    void deleteCommentUnauthorized() throws Exception {
        // when & then
        mockMvc.perform(delete("/comments/{commentId}", commentId)
                        .header("Authorization", "Bearer " + token1)) // 게시글 작성자가 다른 사람의 댓글 삭제 시도
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("댓글 삭제 권한이 없습니다.s")));
    }

    @Test
    @DisplayName("댓글 삭제 API 통합 테스트 - 댓글 없음")
    void deleteCommentNotFound() throws Exception {
        // when & then
        mockMvc.perform(delete("/comments/{commentId}", 9999L)
                        .header("Authorization", "Bearer " + token2))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}