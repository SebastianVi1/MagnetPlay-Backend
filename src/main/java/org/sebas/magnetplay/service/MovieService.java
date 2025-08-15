package org.sebas.magnetplay.service;

import org.sebas.magnetplay.dto.ErrorResponseDto;
import org.sebas.magnetplay.dto.MovieDto;
import org.sebas.magnetplay.exceptions.InvalidDataException;
import org.sebas.magnetplay.exceptions.MovieNotFoundException;
import org.sebas.magnetplay.mapper.MovieMapper;
import org.sebas.magnetplay.model.Movie;
import org.sebas.magnetplay.repo.MovieRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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
        List<Movie> movieList = repo.findAll();
        List<MovieDto> movieDtos = new ArrayList<MovieDto>();

        movieDtos = movieList.stream()
                .map((model) -> mapper.toDto(model))
                .toList();
        return new ResponseEntity<List<MovieDto>>(movieDtos, HttpStatus.OK);

    }

    public ResponseEntity<MovieDto> getMovieById(Long id) {
        Optional<Movie> movie = repo.findById(id);
        if (movie.isEmpty()){
            throw new MovieNotFoundException("Movie with the id: %d not found".formatted(id));
        }
        return new ResponseEntity<MovieDto>(mapper.toDto(movie.get()), HttpStatus.OK);
    }

    public ResponseEntity<MovieDto> createMovie(MovieDto movieDto){
        if (movieDto == null){
            throw new InvalidDataException("Movie cannot be null");
        }
        Movie movie = repo.save(mapper.toModel(movieDto));

        return new ResponseEntity<>(mapper.toDto(movie), HttpStatus.CREATED);
    }

    public ResponseEntity<?> updateMovie(Long movieId, MovieDto updatedMovie){
        if (repo.findById(movieId).isEmpty()){
            throw new MovieNotFoundException("Movie with the id: %d not found".formatted(movieId));
        }
        MovieDto result = mapper.toDto(
                repo.save(mapper.toModel(updatedMovie))
        );

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
