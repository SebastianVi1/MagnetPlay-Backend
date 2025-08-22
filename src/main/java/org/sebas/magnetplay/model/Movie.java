package org.sebas.magnetplay.model;

import jakarta.persistence.*;
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
    @Size(min = 1, max = 100)
    private String date;

    @Size(max=1000)
    private String description;

    @Size(max=500)
    private String extractedFrom;

    private List<String> screenshot;

    private String category;

    @Size(max=500)
    private String poster;

    @Size(min = 0, max = 10000)
    @Column(length = 10000)
    private String magnet;

    @Size(min = 0, max = 2000)
    @Column(length = 2000)
    private String hash;

    private List<String> genres;

}



