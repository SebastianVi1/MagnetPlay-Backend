package org.sebas.magnetplay.init;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sebas.magnetplay.dto.TorrentDataDto;
import org.sebas.magnetplay.dto.TorrentMovieDto;
import org.sebas.magnetplay.model.Movie;
import org.sebas.magnetplay.repo.MovieRepo;
import org.sebas.magnetplay.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TorrentScrapper implements CommandLineRunner {

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public void run(String... args) throws Exception {




    }
}
