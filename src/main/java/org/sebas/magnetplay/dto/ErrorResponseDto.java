package org.sebas.magnetplay.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponseDto {
    private String message;
    private LocalDateTime timestamp;
    private String details;
    private int status;
    public ErrorResponseDto(String message, String details, int status){
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.details = details;
        this.status = status;
    }

}
