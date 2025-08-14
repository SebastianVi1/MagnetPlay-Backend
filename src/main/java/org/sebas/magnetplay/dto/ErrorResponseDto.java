package org.sebas.magnetplay.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponseDto {
    private String message;
    private LocalDateTime timestamp;
    private String details;

    public ErrorResponseDto(String message, String details){
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }

}
