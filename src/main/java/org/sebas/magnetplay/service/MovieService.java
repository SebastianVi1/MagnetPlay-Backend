package org.sebas.magnetplay.service;

import org.sebas.magnetplay.dto.MovieDto;
import org.sebas.magnetplay.exceptions.CategoryNotFoundException;
import org.sebas.magnetplay.exceptions.InvalidDataException;
import org.sebas.magnetplay.exceptions.MovieNotFoundException;
import org.sebas.magnetplay.mapper.MovieMapper;
import org.sebas.magnetplay.model.Movie;
import org.sebas.magnetplay.model.MovieCategory;
import org.sebas.magnetplay.repo.MovieRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;


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

    public ResponseEntity<Map<MovieCategory,List<MovieDto>>> getMoviesOrderedByCategory() throws CategoryNotFoundException {

       List<MovieDto> fetchMovies = mapper.toDtoLit(repo.findAll());

        EnumMap<MovieCategory, List<MovieDto>> filteredMovies = new EnumMap<MovieCategory,List<MovieDto>>(MovieCategory.class);
        filteredMovies.put(MovieCategory.ACTION, new ArrayList<MovieDto>());
        filteredMovies.put(MovieCategory.COMEDY, new ArrayList<MovieDto>());
        filteredMovies.put(MovieCategory.HORROR, new ArrayList<MovieDto>());
        filteredMovies.put(MovieCategory.ROMANCE, new ArrayList<MovieDto>());
        filteredMovies.put(MovieCategory.FICTION, new ArrayList<MovieDto>());

       for (MovieDto movie : fetchMovies){
           for (MovieCategory category: movie.getCategories()){
               switch (category){
                   case ACTION -> filteredMovies.get(MovieCategory.ACTION).add(movie);
                   case COMEDY -> filteredMovies.get(MovieCategory.COMEDY).add(movie);
                   case HORROR -> filteredMovies.get(MovieCategory.HORROR).add(movie);
                   case ROMANCE -> filteredMovies.get(MovieCategory.ROMANCE).add(movie);
                   case FICTION -> filteredMovies.get(MovieCategory.FICTION).add(movie);
                   default -> throw new CategoryNotFoundException("The category is nonexistent");
               }
           }
       }

        return new ResponseEntity<>(filteredMovies, HttpStatus.OK);


    }

    public ResponseEntity<MovieDto> getMovieById(Long id) {
        Optional<Movie> movie = repo.findById(id);
        if (movie.isEmpty()){
            throw new MovieNotFoundException("Movie with the id: %d not found".formatted(id));
        }
        return new ResponseEntity<MovieDto>(mapper.toDto(movie.get()), HttpStatus.OK);
    }

    public ResponseEntity<MovieDto> createMovie(MovieDto movieDto) throws InvalidDataException {
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

    public ResponseEntity<?> deleteMovie(Long id) throws MovieNotFoundException {
        Optional<Movie> movieOptional = repo.findById(id);
        if (movieOptional.isEmpty()){
            throw new MovieNotFoundException("Movie with the id: %d not found".formatted(id));
        }
        Movie movie = movieOptional.get();
        repo.delete(movie);

        return new ResponseEntity<>("Movie %s deleted".formatted(movie.getName()), HttpStatus.OK);
    }

}
