package com.ricklee.community.exception.handler;

import com.ricklee.community.dto.common.ApiResponse;
import com.ricklee.community.exception.custom.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * 애플리케이션 전체의 예외를 처리하는 글로벌 예외 핸들러
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * BusinessException 및 그 하위 클래스의 모든 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException e) {
        // 로깅
        log.warn("Business exception occurred: {}", e.getMessage());

        // BusinessException에 정의된 상태 코드와 에러 코드 사용
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(ApiResponse.error(e.getMessage(), e.getErrorCode()));
    }

    /**
     * ResourceNotFoundException 예외 처리
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage(), e.getErrorCode()));
    }

    /**
     * DuplicateResourceException 예외 처리
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateResourceException(DuplicateResourceException e) {
        log.warn("Duplicate resource: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(e.getMessage(), "DUPLICATE_RESOURCE"));
    }

    /**
     * UnauthorizedException 예외 처리
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<?>> handleUnauthorizedException(UnauthorizedException e) {
        log.warn("Unauthorized access: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getMessage(), "UNAUTHORIZED"));
    }

    /**
     * JWT 관련 모든 예외 처리
     */
    @ExceptionHandler({JwtException.class, TokenExpiredException.class, InvalidTokenException.class, InsufficientTokenPermissionException.class})
    public ResponseEntity<ApiResponse<?>> handleJwtException(RuntimeException e) {
        log.warn("JWT exception: {}", e.getMessage());

        String errorCode = "JWT_ERROR";
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        if (e instanceof TokenExpiredException) {
            errorCode = "TOKEN_EXPIRED";
        } else if (e instanceof InvalidTokenException) {
            errorCode = "INVALID_TOKEN";
        } else if (e instanceof InsufficientTokenPermissionException) {
            errorCode = "INSUFFICIENT_PERMISSION";
            status = HttpStatus.FORBIDDEN;
        }

        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(e.getMessage(), errorCode));
    }

    /**
     * 메서드 인자 유효성 검증 실패 예외 처리
     */
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation error: {}", errors);

        ApiResponse<?> response = ApiResponse.error("Validation failed", "VALIDATION_ERROR", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 바인딩 예외 처리
     */
    protected ResponseEntity<Object> handleBindException(
            BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Binding error: {}", errors);

        ApiResponse<?> response = ApiResponse.error("Binding failed", "BINDING_ERROR", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 메서드 인자 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch: {}", e.getMessage());

        String message = String.format("Parameter '%s' should be of type %s",
                e.getName(), e.getRequiredType().getSimpleName());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message, "TYPE_MISMATCH"));
    }

    /**
     * 필수 헤더 누락 예외 처리
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        log.warn("Missing header: {}", e.getMessage());

        String message = String.format("Required header '%s' is missing", e.getHeaderName());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message, "MISSING_HEADER"));
    }

    /**
     * 핸들러를 찾을 수 없는 예외 처리 (404 Not Found)
     */
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        String message = String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL());
        log.warn(message);

        ApiResponse<?> response = ApiResponse.error(message, "ENDPOINT_NOT_FOUND");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * 기타 모든 예외 처리 (서버 오류)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAllUncaughtException(Exception e) {
        // 예상치 못한 오류는 스택 트레이스까지 로깅
        log.error("Uncaught exception occurred", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred", "INTERNAL_SERVER_ERROR"));
    }
}