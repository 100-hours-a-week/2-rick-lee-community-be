package com.ricklee.community.exception;

/**
 * 이미 존재하는 데이터를 중복 생성하려고 할 때 발생하는 예외
 */
public class DuplicateResourceException extends RuntimeException {

    private final String resourceType;
    private final String fieldName;
    private final Object fieldValue;

    public DuplicateResourceException(String resourceType, String fieldName, Object fieldValue) {
        super(String.format("이미 존재하는 %s입니다. %s: %s", resourceType, fieldName, fieldValue));
        this.resourceType = resourceType;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public DuplicateResourceException(String message) {
        super(message);
        this.resourceType = null;
        this.fieldName = null;
        this.fieldValue = null;
    }
}