package org.sebas.magnetplay.exceptions;

import org.sebas.magnetplay.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionsHandler {

    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<?> handleMovieNotFoundException(MovieNotFoundException exception) {
        ErrorResponseDto movieNotFound = new ErrorResponseDto(
                exception.getMessage(),
                "Resource Not Found Exception",
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(movieNotFound, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<?> handleInvalidDataException(InvalidDataException exception){
        ErrorResponseDto invalidData = new ErrorResponseDto(
                exception.getMessage(),
                "The data passed is invalid",
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(invalidData, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameTakenException.class)
    public ResponseEntity<?> handleUsernameTakenException(UsernameTakenException exception){
        ErrorResponseDto usernameTaken = new ErrorResponseDto(
                exception.getMessage(),
                "The username is already in use",
                HttpStatus.CONFLICT.value()
        );
        return new ResponseEntity<>(usernameTaken, HttpStatus.CONFLICT);
    }
}
