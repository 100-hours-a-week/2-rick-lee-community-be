package com.ricklee.community.exception;

/**
 * 인증되지 않은 사용자의 접근 또는 권한이 없는 작업 시도 시 발생하는 예외
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException() {
        super("인증되지 않은 사용자입니다.");
    }
}