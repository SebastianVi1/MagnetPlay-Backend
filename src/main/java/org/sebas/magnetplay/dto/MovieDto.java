package org.sebas.magnetplay.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MovieDto {

    private Long id;
    @NotNull(message = "The name of the movie can't be null")
    @NotBlank
    private String name;
    private String description;
    private String imageUri;
    private String magnetUri;

}
