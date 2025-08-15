package org.sebas.magnetplay.controller;

import jakarta.validation.Valid;
import org.sebas.magnetplay.dto.MovieDto;
import org.sebas.magnetplay.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MovieController {


    private MovieService service;

    @Autowired
    public MovieController(MovieService movieService){
        this.service = movieService;
    }

    @GetMapping("/movies")
    public ResponseEntity<List<MovieDto>> getMovies(){
        return service.getMovies();
    }

    @GetMapping("/movies/{movieId}")
    public ResponseEntity<?> getMovieById(@PathVariable Long movieId){ // return MovieDto if succes else ErrorResponse
        return service.getMovieById(movieId);
    }

    @PostMapping("/movies")
    public ResponseEntity<?> createMovie(@Valid @RequestBody  MovieDto movieDto){
        return service.createMovie(movieDto);
    }

    @PutMapping("/movies/{movieId}")
    public ResponseEntity<?> updateMovie(@RequestParam Long movieId, @RequestBody MovieDto updatedMovie){

        return service.updateMovie(movieId, updatedMovie);
    }


}
