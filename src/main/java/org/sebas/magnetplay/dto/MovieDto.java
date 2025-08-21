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
    private String name;
    private String description;
    private String date;
    private List<String> screenshot;
    private String posterUri;
    private String magnetUri;
    private String hash;

}
