package org.sebas.magnetplay.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;


    private String size;

    private String date;

    private String description;

    private String extractedFrom;

    private List<String> screenshot;

    private String category;

    private String poster;

    @Size(min = 0, max = 10000)
    private String magnet;

    @Size(min = 0, max = 1000)
    private String hash;

    private List<String> genres;

}



