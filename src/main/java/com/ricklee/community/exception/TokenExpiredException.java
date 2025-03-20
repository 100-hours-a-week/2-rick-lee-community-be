/**
 * 토큰 만료 예외
 * JWT 토큰이 만료된 경우 발생
 */
package com.ricklee.community.exception;

public class TokenExpiredException extends JwtException {

    public TokenExpiredException(String message) {
        super(message);
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}