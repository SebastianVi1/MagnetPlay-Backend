package org.sebas.magnetplay.controller;

import org.sebas.magnetplay.dto.MovieDto;
import org.sebas.magnetplay.model.Movie;
import org.sebas.magnetplay.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class FavoriteMoviesController {

    private final UserService userService;

    @Autowired
    public FavoriteMoviesController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/{userId}/favorites")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<MovieDto>> getUserFavorites(@PathVariable Long userId) {
        return userService.getMyFavoriteMovies(userId);
    }

    @PostMapping("/users/{userId}/favorites/{movieId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> addMovieToFavorites(@PathVariable Long userId, @PathVariable Long movieId) {
        return userService.addMovieToFavorites(movieId, userId);
    }

    @DeleteMapping("/users/{userId}/favorites/{movieId}")
    public ResponseEntity<?> deleteMovieFromFavorites(@PathVariable Long userId, @PathVariable Long movieId) {
        return userService.deleteMovieFromFavorites(userId, movieId);
    }

    @GetMapping("/users/{userId}/favorites/{movieId}/check")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Boolean> checkIfMovieIsFavorite(@PathVariable Long userId, @PathVariable Long movieId) {
        return userService.checkIfFavorite(userId, movieId);
    }
}
