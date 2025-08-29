package org.sebas.magnetplay.exceptions;

public class InvalidRefreshTokenException extends RuntimeException {
    String message;
    public InvalidRefreshTokenException(String message) {
        super(message);
        this.message = message;
    }
}
