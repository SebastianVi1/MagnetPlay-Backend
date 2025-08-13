package org.sebas.magnetplay.service;

import org.sebas.magnetplay.dto.MovieDto;
import org.sebas.magnetplay.mapper.MovieMapper;
import org.sebas.magnetplay.model.MovieModel;
import org.sebas.magnetplay.repo.MovieRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private MovieMapper mapper;
    private MovieRepo repo;

    @Autowired
    public MovieService(MovieRepo repo, MovieMapper mapper){
        this.repo = repo;
        this.mapper = mapper;
    }

    public ResponseEntity<List<MovieDto>> getMovies(){
        List<MovieModel> movieList = repo.findAll();
        List<MovieDto> movieDtos = new ArrayList<MovieDto>();

        movieDtos = movieList.stream()
                .map((model) -> mapper.toDto(model))
                .toList();
        return new ResponseEntity<List<MovieDto>>(movieDtos, HttpStatus.OK);

    }
}
