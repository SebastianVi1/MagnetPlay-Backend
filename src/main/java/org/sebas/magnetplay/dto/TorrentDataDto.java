package org.sebas.magnetplay.dto;

import lombok.Data;
import org.sebas.magnetplay.model.Movie;

import java.io.Serializable;
import java.util.List;

@Data
public class TorrentDataDto{
    private List<TorrentMovieDto> data;

    private int current_page;

    private int total_pages;

    private String time;

    private Long total;

}
