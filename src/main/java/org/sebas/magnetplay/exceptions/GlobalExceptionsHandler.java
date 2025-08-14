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
        ErrorResponseDto movieNotFound = new ErrorResponseDto(exception.getMessage(), "Movie not found");
        return new ResponseEntity<>(movieNotFound, HttpStatus.NOT_FOUND);
    }
}
