package org.sebas.magnetplay.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class MovieNotFoundException extends RuntimeException {
    private String message;


    public MovieNotFoundException(String message){
        super(message);
        this.message = message;
    }
}
