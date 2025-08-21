package org.sebas.magnetplay.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TorrentMovieDto {
    private String name;

    private String size;

    private String date;

    private String seeders;

    private String leechers;

    private String description;

    private String url;

    private String uploader;

    private List<String> screenshot;

    private String category;

    private List<String> files;

    private String poster;

    private String magnet;

    private String hash;

}



