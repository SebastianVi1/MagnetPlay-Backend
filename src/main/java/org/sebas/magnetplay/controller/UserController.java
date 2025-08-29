package org.sebas.magnetplay.controller;

import jakarta.validation.Valid;
import org.sebas.magnetplay.dto.AuthResponseDto;
import org.sebas.magnetplay.dto.RefreshTokenDto;
import org.sebas.magnetplay.dto.UserDto;
import org.sebas.magnetplay.model.Movie;
import org.sebas.magnetplay.service.JWTService;
import org.sebas.magnetplay.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class UserController {

   private UserService service;
   private JWTService jwtService;

    @Autowired
    public UserController(UserService service, JWTService jwtService){
        this.service = service;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registerNewUser(@RequestBody UserDto user) {
        return service.registerNewUser(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody UserDto user){
        return service.verifyUser(user);
    }

    @PostMapping("/register/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponseDto> registerNewAdminUser(@RequestBody @Valid UserDto user){
        return service.registerNewAdminUser(user);
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> post(@RequestBody String value) {
        return service.isTokenValid(value);
    }

    @PutMapping("/users/{userId}/favorites/{movieId}")
    public ResponseEntity<?> addMovieToFavorites(@PathVariable Long movieId, @PathVariable Long userId){
        return service.addMovieToFavorites(movieId, userId);
    }

    @GetMapping("/users/{userId}/favorites")
    public ResponseEntity<List<Movie>> getMyFavoriteMovies(@PathVariable Long userId){
        return service.getMyFavoriteMovies(userId);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refreshToken(@RequestBody RefreshTokenDto refreshToken) {
        return service.refreshAccessToken(refreshToken);
    }

}
