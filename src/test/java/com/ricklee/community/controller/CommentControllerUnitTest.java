package com.ricklee.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ricklee.community.dto.comment.CommentRequestDto;
import com.ricklee.community.dto.common.ApiResponse;
import com.ricklee.community.exception.custom.ResourceNotFoundException;
import com.ricklee.community.exception.custom.UnauthorizedException;
import com.ricklee.community.exception.custom.BusinessException;
import com.ricklee.community.exception.handler.GlobalExceptionHandler;
import com.ricklee.community.service.CommentService;
import com.ricklee.community.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)  // 불필요한 스터빙 경고 방지
public class CommentControllerUnitTest {

    @Mock
    private CommentService commentService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CommentController commentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private final String VALID_TOKEN = "Bearer valid_token";
    private final Long VALID_USER_ID = 1L;
    private final Long VALID_POST_ID = 1L;
    private final Long VALID_COMMENT_ID = 1L;

    @BeforeEach
    void setUp() {
        // GlobalExceptionHandler 인스턴스 생성
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        // MockMvc 설정에 예외 핸들러 추가
        mockMvc = MockMvcBuilders.standaloneSetup(commentController)
                .setControllerAdvice(exceptionHandler)  // 여기서 예외 핸들러 등록
                .build();

        objectMapper = new ObjectMapper();

        // 기본 토큰 검증 설정
        when(userService.getUserIdFromToken(VALID_TOKEN.replace("Bearer ", "")))
                .thenReturn(VALID_USER_ID);
    }
    @Test
    @DisplayName("댓글 작성 API 테스트 - 성공")
    void createCommentSuccess() throws Exception {
        // given
        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setPostId(VALID_POST_ID);
        requestDto.setContent("테스트 댓글 내용");

        given(commentService.createComment(eq(VALID_USER_ID), any()))
                .willReturn(VALID_COMMENT_ID);

        // when & then
        mockMvc.perform(post("/comments")
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("comment_created")))
                .andExpect(jsonPath("$.data.comment_id", is(VALID_COMMENT_ID.intValue())));
    }

    @Test
    @DisplayName("댓글 작성 API 테스트 - 게시글 없음")
    void createCommentPostNotFound() throws Exception {
        // given
        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setPostId(999L);
        requestDto.setContent("테스트 댓글 내용");

        given(commentService.createComment(eq(VALID_USER_ID), any()))
                .willThrow(new ResourceNotFoundException("Post", "id", 999L));

        // when & then
        mockMvc.perform(post("/comments")
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Post not found with id : '999'")))
                .andExpect(jsonPath("$.error.code", is("Post_NOT_FOUND")));
    }

    @Test
    @DisplayName("댓글 작성 API 테스트 - 인증 실패")
    void createCommentUnauthorized() throws Exception {
        // given
        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setPostId(VALID_POST_ID);
        requestDto.setContent("테스트 댓글 내용");

        when(userService.getUserIdFromToken("invalid_token"))
                .thenThrow(new UnauthorizedException("유효하지 않은 토큰입니다."));

        // when & then
        mockMvc.perform(post("/comments")
                        .header("Authorization", "Bearer invalid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("유효하지 않은 토큰입니다.")))
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    @DisplayName("댓글 목록 조회 API 테스트 - 성공")
    void getCommentsSuccess() throws Exception {
        // given
        List<Map<String, Object>> commentsList = new ArrayList<>();
        Map<String, Object> comment1 = new HashMap<>();
        comment1.put("id", 1L);
        comment1.put("content", "첫 번째 댓글");
        comment1.put("author", "user1");

        Map<String, Object> comment2 = new HashMap<>();
        comment2.put("id", 2L);
        comment2.put("content", "두 번째 댓글");
        comment2.put("author", "user2");

        commentsList.add(comment1);
        commentsList.add(comment2);

        given(commentService.getCommentsByPostId(VALID_POST_ID))
                .willReturn(commentsList);

        // when & then
        mockMvc.perform(get("/posts/{postId}/comments", VALID_POST_ID)
                        .header("Authorization", VALID_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("comments_retrieved")))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id", is(1)))
                .andExpect(jsonPath("$.data[1].content", is("두 번째 댓글")));
    }

    @Test
    @DisplayName("댓글 목록 조회 API 테스트 - 게시글 없음")
    void getCommentsPostNotFound() throws Exception {
        // given
        given(commentService.getCommentsByPostId(999L))
                .willThrow(new ResourceNotFoundException("Post", "id", 999L));

        // when & then
        mockMvc.perform(get("/posts/{postId}/comments", 999L)
                        .header("Authorization", VALID_TOKEN))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Post not found with id : '999'")))
                .andExpect(jsonPath("$.error.code", is("Post_NOT_FOUND")));
    }

    @Test
    @DisplayName("댓글 수정 API 테스트 - 성공")
    void updateCommentSuccess() throws Exception {
        // given
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("content", "수정된 댓글 내용");

        Map<String, Object> updatedComment = new HashMap<>();
        updatedComment.put("id", VALID_COMMENT_ID);
        updatedComment.put("content", "수정된 댓글 내용");
        updatedComment.put("updated_at", "2023-01-01T12:00:00");

        given(commentService.updateComment(eq(VALID_USER_ID), eq(VALID_COMMENT_ID), eq("수정된 댓글 내용")))
                .willReturn(updatedComment);

        // when & then
        mockMvc.perform(put("/comments/{commentId}", VALID_COMMENT_ID)
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("comment_updated")))
                .andExpect(jsonPath("$.data.id", is(VALID_COMMENT_ID.intValue())))
                .andExpect(jsonPath("$.data.content", is("수정된 댓글 내용")));
    }

    @Test
    @DisplayName("댓글 수정 API 테스트 - 댓글 없음")
    void updateCommentNotFound() throws Exception {
        // given
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("content", "수정된 댓글 내용");

        given(commentService.updateComment(eq(VALID_USER_ID), eq(999L), anyString()))
                .willThrow(new ResourceNotFoundException("Comment", "id", 999L));

        // when & then
        mockMvc.perform(put("/comments/{commentId}", 999L)
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Comment not found with id : '999'")))
                .andExpect(jsonPath("$.error.code", is("Comment_NOT_FOUND")));
    }

    @Test
    @DisplayName("댓글 수정 API 테스트 - 권한 없음")
    void updateCommentUnauthorized() throws Exception {
        // given
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("content", "수정된 댓글 내용");

        given(commentService.updateComment(eq(VALID_USER_ID), eq(VALID_COMMENT_ID), anyString()))
                .willThrow(new UnauthorizedException("댓글 작성자만 수정할 수 있습니다."));

        // when & then
        mockMvc.perform(put("/comments/{commentId}", VALID_COMMENT_ID)
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("댓글 작성자만 수정할 수 있습니다.")))
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }

    @Test
    @DisplayName("댓글 수정 API 테스트 - 빈 내용")
    void updateCommentEmptyContent() throws Exception {
        // given
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("content", "");

        // when & then
        mockMvc.perform(put("/comments/{commentId}", VALID_COMMENT_ID)
                        .header("Authorization", VALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 삭제 API 테스트 - 성공")
    void deleteCommentSuccess() throws Exception {
        // given
        doNothing().when(commentService).deleteComment(VALID_USER_ID, VALID_COMMENT_ID);

        // when & then
        mockMvc.perform(delete("/comments/{commentId}", VALID_COMMENT_ID)
                        .header("Authorization", VALID_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("comment_deleted")))
                .andExpect(jsonPath("$.data.comment_id", is(VALID_COMMENT_ID.intValue())));
    }

    @Test
    @DisplayName("댓글 삭제 API 테스트 - 댓글 없음")
    void deleteCommentNotFound() throws Exception {
        // given
        doThrow(new ResourceNotFoundException("Comment", "id", 999L))
                .when(commentService).deleteComment(VALID_USER_ID, 999L);

        // when & then
        mockMvc.perform(delete("/comments/{commentId}", 999L)
                        .header("Authorization", VALID_TOKEN))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Comment not found with id : '999'")))
                .andExpect(jsonPath("$.error.code", is("Comment_NOT_FOUND")));
    }

    @Test
    @DisplayName("댓글 삭제 API 테스트 - 권한 없음")
    void deleteCommentUnauthorized() throws Exception {
        // given
        doThrow(new UnauthorizedException("댓글 작성자만 삭제할 수 있습니다."))
                .when(commentService).deleteComment(VALID_USER_ID, VALID_COMMENT_ID);

        // when & then
        mockMvc.perform(delete("/comments/{commentId}", VALID_COMMENT_ID)
                        .header("Authorization", VALID_TOKEN))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("댓글 작성자만 삭제할 수 있습니다.")))
                .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
    }
}