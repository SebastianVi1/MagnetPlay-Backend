package org.sebas.magnetplay.exceptions;

import lombok.Data;

@Data
public class InvalidDataException extends RuntimeException {
    private String message;

    public InvalidDataException(String message){
        this.message = message;
    }

}
