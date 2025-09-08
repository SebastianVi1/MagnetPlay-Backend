package org.sebas.magnetplay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sebas.magnetplay.dto.AuthResponseDto;
import org.sebas.magnetplay.dto.UserDto;
import org.sebas.magnetplay.mapper.UserMapper;
import org.sebas.magnetplay.model.Movie;
import org.sebas.magnetplay.model.Role;
import org.sebas.magnetplay.model.Users;
import org.sebas.magnetplay.repo.MovieRepo;
import org.sebas.magnetplay.repo.RoleRepo;
import org.sebas.magnetplay.repo.UsersRepo;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UsersRepo userRepo;

    @Mock
    private MovieRepo movieRepo;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleRepo roleRepo;

    @Mock
    private Authentication authentication;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JWTService jwtService;

    private Users testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUserDto = new UserDto();
        testUserDto.setUsername("testName");
        testUserDto.setPassword("password");
        testUserDto.setEmail("email22@example.com");

        testUser = new Users();
        testUser.setUsername("testName");
        testUser.setPassword("password");
        testUser.setEmail("email22@example.com");

    }

    @Test
    void shouldRegisterANewUser(){
        //Given
        // Emulate the behavior of the mocks
        when(userRepo.save(any(Users.class))).thenReturn(testUser);
        when(userMapper.toModel(any(UserDto.class))).thenReturn(testUser);
        when(userMapper.toDto(any(Users.class))).thenReturn(testUserDto);
        when(roleRepo.findByName(any(String.class))).thenReturn(
                Optional.of(
                        new Role(null, "ROLE_USER")
                )
        );
        // When
        var result = userService.registerNewUser(testUserDto);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isInstanceOf(AuthResponseDto.class);
        verify(userRepo).save(any(Users.class));
        verify(roleRepo).findByName(any(String.class));
    }

    @Test
    void shouldReturnJwtTokenOnValidUserVerification() {
        String expectedToken = "mocked.jwt.token";
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(jwtService.generateToken(any(String.class))).thenReturn(expectedToken);

        String actualToken = userService.verifyUser(testUserDto).getBody().getToken();

        assertThat(actualToken).isEqualTo(expectedToken);
        verify(jwtService).generateToken(testUserDto.getUsername());
    }

    @Test
    void shouldAddAMovieToFavoritesList(){
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setName("Test Movie");
        //Given
        when(movieRepo.findById(1L))
                .thenReturn(Optional.of(movie));

        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        //When
        var result = userService.addMovieToFavorites(1L, 1L);

        //Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo("Movie added to favorites successfully");
        verify(movieRepo).findById(1L);
        verify(userRepo).findById(1L);
        verify(userRepo).save(testUser);
    }


    @Test
    void shouldReturnMyFavoriteMovies(){
        //Given
        when(userRepo.findById(1L)).thenReturn(Optional.of(new Users()));

        //When
        var result = userService.getMyFavoriteMovies(1L);

        //Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}
