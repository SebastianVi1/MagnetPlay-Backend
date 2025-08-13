package org.sebas.magnetplay.mapper;

import org.sebas.magnetplay.dto.MovieDto;
import org.sebas.magnetplay.model.MovieModel;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper {
    //Convert entity to Dto for public use
    public MovieDto toDto(MovieModel model){
        MovieDto movieDto = new MovieDto();
        movieDto.setId(model.getId());
        movieDto.setName(model.getName());
        movieDto.setDescription(model.getDescription());
        movieDto.setImageUri(model.getImageUri());
        movieDto.setMagnetUri(model.getMagnetUri());
        return movieDto;
    }

    //Convert dto to entity
    public MovieModel toModel(MovieDto dto){

        MovieModel model = new MovieModel();
        model.setId(dto.getId());
        model.setName(dto.getName());
        model.setDescription(dto.getDescription());
        model.setMagnetUri(dto.getMagnetUri());
        model.setImageUri(dto.getImageUri());

        return model;
    }

}
