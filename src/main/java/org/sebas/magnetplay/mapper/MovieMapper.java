package org.sebas.magnetplay.mapper;

import org.sebas.magnetplay.dto.MovieDto;
import org.sebas.magnetplay.model.Movie;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper {
    //Convert entity to Dto for public use
    public MovieDto toDto(Movie model){
        MovieDto movieDto = new MovieDto();
        movieDto.setId(model.getId());
        movieDto.setName(model.getName());
        movieDto.setDescription(model.getDescription());
        movieDto.setImageUri(model.getImageUri());
        return movieDto;
    }

    //Convert dto to entity
    public Movie toModel(MovieDto dto){

        Movie model = new Movie();
        model.setId(dto.getId());
        model.setName(dto.getName());
        model.setDescription(dto.getDescription());
        model.setImageUri(dto.getImageUri());

        return model;
    }

}
