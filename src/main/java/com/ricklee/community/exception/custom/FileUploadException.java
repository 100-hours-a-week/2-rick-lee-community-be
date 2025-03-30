package com.ricklee.community.exception.custom;

/**
 * 파일 업로드 관련 예외를 처리하는 커스텀 예외 클래스
 */
public class FileUploadException extends RuntimeException {

    /**
     * 메시지와 함께 예외를 생성합니다.
     *
     * @param message 예외 메시지
     */
    public FileUploadException(String message) {
        super(message);
    }

    /**
     * 메시지와 원인 예외와 함께 예외를 생성합니다.
     *
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}