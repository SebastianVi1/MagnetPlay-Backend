package org.sebas.magnetplay.exceptions;

import lombok.Data;

@Data
public class CategoryNotFoundException extends RuntimeException {
    private String message;
    public CategoryNotFoundException(String message) {
        super(message);
        this.message = message;
    }
}
