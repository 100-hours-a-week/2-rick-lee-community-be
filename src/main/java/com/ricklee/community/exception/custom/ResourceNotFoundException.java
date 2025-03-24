package com.ricklee.community.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 요청한 리소스를 찾을 수 없을 때 발생하는 예외
 */
@Getter
public class ResourceNotFoundException extends BusinessException {

    private final String resourceType;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceType, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceType, fieldName, fieldValue),
                resourceType + "_NOT_FOUND",
                HttpStatus.NOT_FOUND);
        this.resourceType = resourceType;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public ResourceNotFoundException(String resourceType) {
        super(String.format("%s not found", resourceType),
                resourceType + "_NOT_FOUND",
                HttpStatus.NOT_FOUND);
        this.resourceType = resourceType;
        this.fieldName = null;
        this.fieldValue = null;
    }
}