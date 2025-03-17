package com.ricklee.community.exception;

import lombok.Getter;

/**
 * 요청한 리소스를 찾을 수 없을 때 발생하는 예외
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceType, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceType, fieldName, fieldValue));
        this.resourceType = resourceType;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public ResourceNotFoundException(String resourceType) {
        super(String.format("%s not found", resourceType));
        this.resourceType = resourceType;
        this.fieldName = null;
        this.fieldValue = null;
    }
}