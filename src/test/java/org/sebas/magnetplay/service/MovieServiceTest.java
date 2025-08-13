package org.sebas.magnetplay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sebas.magnetplay.dto.MovieDto;
import org.sebas.magnetplay.mapper.MovieMapper;
import org.sebas.magnetplay.model.MovieModel;
import org.sebas.magnetplay.repo.MovieRepo;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {

    @Mock
    MovieRepo repo;

    @InjectMocks
    MovieService service;

    @Mock
    MovieMapper mapper;

    @Test
    void shouldReturnAListWithStatusOk(){
        when(repo.findAll()).thenReturn(List.of(
                new MovieModel()
        ));

        when(mapper.toDto(new MovieModel())).thenReturn(new MovieDto());
        var result = service.getMovies();
        assertThat(result.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(result.getBody()).hasSize(1);
        assertThat(result.getBody()).allMatch(Objects::nonNull);
        verify(repo).findAll();


    }

}
