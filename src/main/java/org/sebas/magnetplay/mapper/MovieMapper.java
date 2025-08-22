package org.sebas.magnetplay.mapper;

import org.sebas.magnetplay.dto.MovieDto;
import org.sebas.magnetplay.dto.TorrentDataDto;
import org.sebas.magnetplay.dto.TorrentMovieDto;
import org.sebas.magnetplay.model.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MovieMapper {

    //Convert entity to Dto for public use
    public MovieDto toDto(Movie model){
        MovieDto movieDto = new MovieDto();
        movieDto.setId(model.getId());
        movieDto.setName(model.getName());
        movieDto.setDescription(model.getDescription());
        movieDto.setDate(model.getDate());
        movieDto.setHash(model.getHash());
        movieDto.setMagnetUri(model.getMagnet());
        movieDto.setScreenshot(model.getScreenshot());
        movieDto.setPosterUri(model.getPoster());
        movieDto.setCategory(model.getCategory());
        movieDto.setGenres(model.getGenres());
        return movieDto;
    }

    //Convert dto to entity
    public Movie toModel(MovieDto dto){

        Movie model = new Movie();
        model.setId(dto.getId());
        model.setName(dto.getName());
        model.setDescription(dto.getDescription());
        model.setScreenshot(dto.getScreenshot());
        model.setDate(dto.getDate());
        model.setHash(dto.getHash());
        model.setPoster(dto.getPosterUri());
        model.setMagnet(dto.getMagnetUri());
        model.setCategory(dto.getCategory());
        model.setGenres(dto.getGenres());

        return model;
    }

    // Return a list with MovieDto
    public List<MovieDto> toDtoList(List<Movie> list){
        return list.stream().map(this::toDto).toList(); // convert all entities to dot
    }


    public Movie fromTorrentToMovie(TorrentMovieDto torrentMovieDto){
        Movie movie = new Movie();
        movie.setName(torrentMovieDto.getName());
        movie.setCategory(torrentMovieDto.getCategory());
        movie.setHash(torrentMovieDto.getHash());
        movie.setDate(torrentMovieDto.getDate());
        movie.setMagnet(torrentMovieDto.getMagnet());
        movie.setPoster(torrentMovieDto.getPoster());
        movie.setSize(torrentMovieDto.getSize());
        movie.setExtractedFrom(torrentMovieDto.getUrl());
        movie.setScreenshot(torrentMovieDto.getScreenshot());
        return movie;
    }

}
