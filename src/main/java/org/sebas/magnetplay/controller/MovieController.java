package org.sebas.magnetplay.controller;

import jakarta.validation.Valid;
import org.sebas.magnetplay.dto.MovieDto;
import org.sebas.magnetplay.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin("http://localhost:5173")
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

    @GetMapping("/movies/categories")
    public ResponseEntity<Map<String,List<MovieDto>>> getMoviesByCategory(){
        return service.getOrderedByCategory();
    }

    @GetMapping("/movies/recent")
    public ResponseEntity<?> getRecentMovies(){
        return service.getRecentMovies();
    }

    @PreAuthorize("hasAnyRole('USER, ADMIN')")
    @GetMapping("/movies/{movieId}")
    public ResponseEntity<?> getMovieById(@PathVariable Long movieId){ // return MovieDto if succes else ErrorResponse
        return service.getMovieById(movieId);
    }

    @PostMapping("/movies")
    @PreAuthorize("hasRole('ADMIN')")
   public ResponseEntity<?> createMovie(@Valid @RequestBody  MovieDto movieDto){
        return service.createMovie(movieDto);
    }

    @PutMapping("/movies/{movieId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateMovie(@Valid @PathVariable Long movieId, @RequestBody @Valid MovieDto updatedMovie){

        return service.updateMovie(movieId, updatedMovie);
    }

    @DeleteMapping("/movies/{movieId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteMovie(@PathVariable Long movieId){
        return service.deleteMovie(movieId);
    }

    @GetMapping("movies/{id}/stream")
    public ResponseEntity<?> streamMovie(@PathVariable Long id){
        return service.streamMovie(id);
    }
}
