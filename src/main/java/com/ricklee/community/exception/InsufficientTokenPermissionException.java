/**
 * 토큰 권한 부족 예외
 * 토큰의 권한이 필요한 작업을 수행하기에 부족한 경우 발생
 */
package com.ricklee.community.exception;

public class InsufficientTokenPermissionException extends JwtException {

    public InsufficientTokenPermissionException(String message) {
        super(message);
    }

    public InsufficientTokenPermissionException(String message, Throwable cause) {
        super(message, cause);
    }
}