package com.ricklee.community.exception.custom;

/**
 * JWT 관련 기본 예외
 * JWT 처리 중 발생하는 모든 예외의 기본 클래스
 */
public class JwtException extends RuntimeException {

    public JwtException(String message) {
        super(message);
    }

    public JwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
