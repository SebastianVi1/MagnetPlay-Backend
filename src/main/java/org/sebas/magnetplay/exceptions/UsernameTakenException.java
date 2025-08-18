package org.sebas.magnetplay.exceptions;

import lombok.Data;

@Data
public class UsernameTakenException extends RuntimeException {
    private String message;
    public UsernameTakenException(String message) {
        super(message);
        this.message = message;
    }
}
