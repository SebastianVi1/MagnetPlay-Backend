package org.sebas.magnetplay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.sebas.magnetplay.model.MovieCategory;
import java.util.ArrayList;
import java.util.List;

@Data
public class MovieDto {

    private Long id;
    @NotNull(message = "The name of the movie can't be null")
    @NotBlank
    private String name;
    private String description;
    private String imageUri;
    private List<MovieCategory> categories = new ArrayList<>();

}
