package org.sebas.magnetplay.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbMovieDetails {

    private int id;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("vote_average")
    private double voteAverage;

    private String overview;

    @JsonProperty("release_date")
    private String releaseDate;

    private int runtime;

    private List<TmdbGenre> genres;
}
