package org.sebas.magnetplay.dto;

import lombok.Data;
import java.util.List;

@Data
public class MovieDto {

    private Long id;
    private String name;
    private String description;
    private String date;
    private List<String> screenshot;
    private String category;
    private String posterUri;
    private String magnetUri;
    private String hash;
    private List<String> genres;

}
