/**
 * 유효하지 않은 토큰 예외
 * 서명이 잘못되었거나, 형식이 올바르지 않은 경우 발생
 */
package com.ricklee.community.exception.custom;

public class InvalidTokenException extends JwtException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}