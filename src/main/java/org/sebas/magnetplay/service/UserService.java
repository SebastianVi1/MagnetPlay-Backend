package org.sebas.magnetplay.service;

import org.sebas.magnetplay.dto.AuthResponseDto;
import org.sebas.magnetplay.dto.MovieDto;
import org.sebas.magnetplay.dto.UserDto;
import org.sebas.magnetplay.dto.RefreshTokenDto;
import org.sebas.magnetplay.exceptions.InvalidRefreshTokenException;
import org.sebas.magnetplay.exceptions.MovieNotFoundException;
import org.sebas.magnetplay.exceptions.UserNotFoundException;
import org.sebas.magnetplay.exceptions.UsernameTakenException;
import org.sebas.magnetplay.mapper.MovieMapper;
import org.sebas.magnetplay.mapper.UserMapper;
import org.sebas.magnetplay.model.Movie;
import org.sebas.magnetplay.model.Role;
import org.sebas.magnetplay.model.Users;
import org.sebas.magnetplay.repo.MovieRepo;
import org.sebas.magnetplay.repo.RoleRepo;
import org.sebas.magnetplay.repo.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.stream.events.Comment;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Collections;
import java.util.ArrayList;


@Service
public class UserService {


    private MovieRepo movieRepo;

    private JWTService jwtService;

    private RoleRepo roleRepo;

    private UserMapper userMapper;

    private UsersRepo usersRepo;

    private MovieMapper movieMapper;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    AuthenticationManager authManager;

    @Autowired
    public UserService(UsersRepo usersRepo, AuthenticationManager authManager, JWTService jwtService, RoleRepo roleRepo, UserMapper userMapper, MovieRepo movieRepo, MovieMapper movieMapper){
        this.jwtService = jwtService;
        this.authManager = authManager;
        this.usersRepo = usersRepo;
        this.roleRepo = roleRepo;
        this.userMapper = userMapper;
        this.movieRepo = movieRepo;
        this.movieMapper = movieMapper;
    }

    private AuthResponseDto buildAuthResponse(Users user) {
        String token = jwtService.generateToken(user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());
        System.out.println(refreshToken);
        UserDto responseUserDto = userMapper.toDto(user);
        return new AuthResponseDto(responseUserDto, token, refreshToken);
    }

    public ResponseEntity<AuthResponseDto> registerNewUser(UserDto userDto){
        // Convert UserDto to Entity
        Users user = userMapper.toModel(userDto);

        // Verify if the username exists
        if (usersRepo.findByUsername(user.getUsername()) != null){
            throw new UsernameTakenException("The username: %s is already in use".formatted(user.getUsername()));
        }

        Role role = roleRepo.findByName("ROLE_USER").get(); //assign user rol by default
        user.setRoles(Set.of(role));
        user.setPassword(encoder.encode(user.getPassword())); // encrypt the password
        //save the new user
        usersRepo.save(user);
        AuthResponseDto authResponse = buildAuthResponse(user);
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    public ResponseEntity<AuthResponseDto> registerNewAdminUser(UserDto userDto){
        Users user = userMapper.toModel(userDto);
        // Verify if the username exists
        if (usersRepo.findByUsername(user.getUsername()) != null){
            throw new UsernameTakenException("The username: %s is already in use".formatted(user.getUsername()));
        }
        List<Role> roles = roleRepo.findAll();
        user.setRoles(
                Set.of(
                        roles.get(0),
                        roles.get(1)
                )
        );
        user.setPassword(encoder.encode(user.getPassword())); // encrypt the password
        usersRepo.save(user);
        AuthResponseDto authResponse = buildAuthResponse(user);
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    public ResponseEntity<AuthResponseDto> verifyUser(UserDto user) {
        Authentication authentication =
                authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

        if (authentication.isAuthenticated()){
            String token =  jwtService.generateToken(user.getUsername());
            String refreshToken = jwtService.generateRefreshToken(user.getUsername());
            UserDto dbUser = userMapper.toDto(usersRepo.findByUsername(user.getUsername()));
            AuthResponseDto response = new AuthResponseDto(dbUser, token, refreshToken);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        throw new BadCredentialsException("The autentication failed");
    }

    public ResponseEntity<Boolean> isTokenValid(String token) {
        if (token == null || token.isEmpty()){
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
             boolean response = jwtService.isTokenValid(token);
            return response
                    ? new ResponseEntity<Boolean>(true, HttpStatus.OK)
                    : new ResponseEntity<Boolean>(false, HttpStatus.UNAUTHORIZED);

    }


    @Transactional
    public ResponseEntity<?> addMovieToFavorites(Long movieId, Long userId ) {
        Optional<Movie> movieOptional = movieRepo.findById(movieId);

        if (movieOptional.isEmpty()){
            throw new MovieNotFoundException("Movie with the id: %d not found".formatted(movieId));
        }
        Users user = usersRepo.findById(userId).orElseThrow(() ->
                new UserNotFoundException("The user with the id: %d not found".formatted(userId)));

        Set<Movie> favoriteMovies = user.getFavoriteMovies();

        // Check if already in favorites
        boolean alreadyExists = favoriteMovies.stream()
                .anyMatch(movie -> movie.getId().equals(movieId));

        if (alreadyExists) {
            return new ResponseEntity<>("Movie already in favorites", HttpStatus.OK);
        }

        favoriteMovies.add(movieOptional.get());
        user.setFavoriteMovies(favoriteMovies);
        usersRepo.save(user);
        return new ResponseEntity<>("Movie added to favorites successfully" , HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<MovieDto>> getMyFavoriteMovies(Long userId) {
        Users user = usersRepo.findById(userId)
                .orElseThrow( () ->
                        new UsernameNotFoundException("The user doesn't exist")
                );

        Set<Movie> favoriteMovies = user.getFavoriteMovies();

        // Convert Set<Movie> to List<Movie> then to List<MovieDto>
        List<Movie> movieList = new ArrayList<>(favoriteMovies);
        List<MovieDto> moviesDto = movieMapper.toDtoList(movieList);

        return new ResponseEntity<>(moviesDto, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> deleteMovieFromFavorites(Long userId, Long movieId){
        // Find the user
        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("The user with the id: %d not found".formatted(userId)));

        // Find the movie
        Optional<Movie> movieOptional = movieRepo.findById(movieId);
        if (movieOptional.isEmpty()) {
            throw new MovieNotFoundException("Movie with the id: %d not found".formatted(movieId));
        }

        // Get favorite movies
        Set<Movie> favoriteMovies = user.getFavoriteMovies();

        // Remove the movie from favorites
        boolean removed = favoriteMovies.removeIf(movie -> movie.getId().equals(movieId));

        if (!removed) {
            return new ResponseEntity<>("Movie was not in favorites", HttpStatus.NOT_FOUND);
        }

        // Update user's favorites
        user.setFavoriteMovies(favoriteMovies);
        usersRepo.save(user);

        return new ResponseEntity<>("Movie removed from favorites successfully", HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Boolean> checkIfFavorite(Long userId, Long movieId){
        Optional<Users> user = usersRepo.findById(userId);

        if (user.isEmpty()) {
            throw new UserNotFoundException("The user with the id " + userId + " doesn't exists");
        }

        Set<Movie> favoriteMovies = user.get().getFavoriteMovies();

        // Check if the movie with the given movieId is in favorites
        boolean isFavorite = favoriteMovies.stream()
                .anyMatch(movie -> movie.getId().equals(movieId));

        return new ResponseEntity<>(isFavorite, HttpStatus.OK);
    }

    public ResponseEntity<AuthResponseDto> refreshAccessToken(RefreshTokenDto refreshTokenDto) {
        if (refreshTokenDto == null || refreshTokenDto.getRefreshToken() == null || refreshTokenDto.getRefreshToken().isEmpty()) {
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }

        String refreshToken = refreshTokenDto.getRefreshToken();
        
        try {
            String username = jwtService.extractUsername(refreshToken);
            System.out.println("Extracted username: " + username);
            
            if (username == null || username.isEmpty()) {
                throw new InvalidRefreshTokenException("Could not extract username from token");
            }
            
            Users user = usersRepo.findByUsername(username);
            if (user == null) {
                throw new UserNotFoundException("User not found for username: " + username);
            }
            
            UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);
            if (!jwtService.isRefreshTokenValid(refreshToken, userDetails)) {
                throw new InvalidRefreshTokenException("Refresh token is not valid");
            }
            
            AuthResponseDto response = buildAuthResponse(user);
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("Error during token refresh: " + e.getMessage());
            throw new InvalidRefreshTokenException("Token refresh failed: " + e.getMessage());
        }
    }

}
