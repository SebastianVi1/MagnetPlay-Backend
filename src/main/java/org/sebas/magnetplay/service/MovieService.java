package org.sebas.magnetplay.service;

import org.sebas.magnetplay.dto.MovieDto;
import org.sebas.magnetplay.dto.TorrentMovieDto;
import org.sebas.magnetplay.exceptions.InvalidDataException;
import org.sebas.magnetplay.exceptions.MovieNotFoundException;
import org.sebas.magnetplay.mapper.MovieMapper;
import org.sebas.magnetplay.model.Movie;
import org.sebas.magnetplay.repo.MovieRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

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
        if (repo.findById(movieDto.getId()).isEmpty()){
            return new ResponseEntity<>(movieDto, HttpStatus.BAD_REQUEST);
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

    public ResponseEntity<?> createTorrentMovie(@RequestBody TorrentMovieDto torrentMovie){
        Movie movieEntity = mapper.fromTorrentToMovie(torrentMovie);
        var newMovie = repo.save(movieEntity);
        return new ResponseEntity<>(newMovie, HttpStatus.CREATED);

    }

    public ResponseEntity<?> getRecentMovies() {
    return null;
    }

    public ResponseEntity<Map<String,List<MovieDto>>> getOrderedByCategory(){
        List<MovieDto> movieList = mapper.toDtoList(repo.findAll());

        HashSet<String> categories = new HashSet<>();
        HashMap<String, List<MovieDto>> categoryMap = new HashMap<>();

        for (MovieDto movie : movieList){
            if (movie.getCategory() == null){
                System.out.println(movie.getCategory());
                continue;
            }
            categories.add(movie.getCategory());
        }
        for (String category: categories){
            categoryMap.put(category, new ArrayList<>());
        }
       for (MovieDto movie : movieList){
           for (String category : categoryMap.keySet()){
               if( category.equals(movie.getCategory())){
                   categoryMap.get(category).add(movie);
               }
           }
       }
       return new ResponseEntity<>(categoryMap, HttpStatus.OK);
    }

}
