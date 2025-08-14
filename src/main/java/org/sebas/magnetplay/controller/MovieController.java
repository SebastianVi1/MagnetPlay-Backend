package org.sebas.magnetplay.controller;

import org.apache.coyote.Response;
import org.sebas.magnetplay.dto.MovieDto;
import org.sebas.magnetplay.exceptions.MovieNotFoundException;
import org.sebas.magnetplay.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MovieController {


    private MovieService movieService;

    @Autowired
    public MovieController(MovieService movieService){
        this.movieService = movieService;
    }

    @GetMapping("/movie")
    public ResponseEntity<List<MovieDto>> getMovies(){
        return movieService.getMovies();
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<?> getMovieById(@PathVariable Long movieId){ // return MovieDto if succes else ErrorResponse
        return movieService.getMovieById(movieId);
    }


}
