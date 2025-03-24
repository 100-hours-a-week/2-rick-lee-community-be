package com.ricklee.community.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API 응답의 표준 형식을 정의하는 클래스
 * 모든 API 응답은 이 클래스를 통해 일관된 형식으로 반환됨
 *
 * @param <T> 응답 데이터의 타입
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값은 JSON 응답에서 제외
public class ApiResponse<T> {

    private String message;
    private T data;
    private ErrorInfo error;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 성공 응답 생성 - 데이터와 메시지 포함
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 성공 응답 생성 - 데이터만 포함, 기본 메시지 사용
     */
    public static <T> ApiResponse<T> success(T data) {
        return success("success", data);
    }

    /**
     * 성공 응답 생성 - 메시지만 포함
     */
    public static <T> ApiResponse<T> success(String message) {
        return success(message, null);
    }

    /**
     * 오류 응답 생성 - 기본
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        ErrorInfo errorInfo = new ErrorInfo(errorCode, message, null);
        return ApiResponse.<T>builder()
                .message(message)
                .error(errorInfo)
                .build();
    }

    /**
     * 오류 응답 생성 - 상세 정보 포함
     */
    public static <T> ApiResponse<T> error(String message, String errorCode, Object details) {
        ErrorInfo errorInfo = new ErrorInfo(errorCode, message, details);
        return ApiResponse.<T>builder()
                .message(message)
                .error(errorInfo)
                .build();
    }

    /**
     * 오류 정보를 담는 내부 클래스
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorInfo {
        private String code;
        private String message;
        private Object details;
    }
}