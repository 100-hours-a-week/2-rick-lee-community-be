package com.ricklee.community.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * API 응답 표준 형식 DTO
 * @param <T> 응답 데이터 타입
 */
@Getter
@Setter
@NoArgsConstructor
public class ApiResponse<T> {
    private String message;
    private T data;

    public ApiResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    /**
     * 성공 응답 생성
     * @param message 응답 메시지
     * @param data 응답 데이터
     * @param <T> 응답 데이터 타입
     * @return API 응답 객체
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data);
    }

    /**
     * 실패 응답 생성
     * @param message 오류 메시지
     * @param <T> 응답 데이터 타입
     * @return API 응답 객체
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(message, null);
    }
}