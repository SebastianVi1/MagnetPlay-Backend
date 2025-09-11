package org.sebas.magnetplay.exceptions;

public class InvalidUserException extends RuntimeException {
    String message;
    public InvalidUserException(String message) {
        super(message);
        this.message = message;
    }
}
