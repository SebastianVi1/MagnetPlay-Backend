package org.sebas.magnetplay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sebas.magnetplay.dto.MovieDto;
import org.sebas.magnetplay.exceptions.MovieNotFoundException;
import org.sebas.magnetplay.mapper.MovieMapper;
import org.sebas.magnetplay.model.Movie;
import org.sebas.magnetplay.repo.MovieRepo;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {

    @Mock
    MovieRepo repo;

    @InjectMocks
    MovieService service;

    @Mock
    MovieMapper mapper;

    private Movie testMovie;
    private MovieDto testMovieDto;

    @BeforeEach
    void  setUp(){
        this.testMovie = new Movie();
        testMovie.setId(1L);
        testMovie.setName("Avengers");
        testMovie.setDescription("Test description...");

        this.testMovieDto = new MovieDto();
        testMovieDto.setName("Avengers Dto");
        testMovieDto.setDescription("DTO description...");
    }

    @Test
    void shouldReturnAListWithStatusOk(){
        //Given
        when(repo.findAll()).thenReturn(List.of(
                new Movie()
        ));

        //When
        when(mapper.toDto(new Movie())).thenReturn(new MovieDto());
        var result = service.getMovies();
        assertThat(result.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        //Then
        assertThat(result.getBody()).hasSize(1);
        assertThat(result.getBody()).allMatch(Objects::nonNull);
        verify(repo).findAll();


    }


    @Test
    void shouldReturnAMovieByHisId(){
        //Given
        when(repo.findById(1L)).thenReturn(Optional.of(testMovie));
        when(mapper.toDto(testMovie)).thenReturn(testMovieDto);

        //When
        var result = service.getMovieById(1L);

        //Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).extracting(MovieDto::getClass).isEqualTo(MovieDto.class);

        verify(repo).findById(1L);

    }

    @Test
    void shouldReturnNotFoundAndException(){

        when(repo.findById(2222L)).thenReturn(Optional.empty());
        // When & Then
        assertThatThrownBy(() -> service.getMovieById(2222L))
                .isInstanceOf(MovieNotFoundException.class)
                .hasMessage("Movie with the id: 2222 not found");

        verify(repo).findById(2222L);


    }

}
