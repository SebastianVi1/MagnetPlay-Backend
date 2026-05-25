package org.sebas.magnetplay.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbSearchResponse {

    private List<TmdbMovieResult> results;

    @JsonProperty("total_results")
    private int totalResults;
}
