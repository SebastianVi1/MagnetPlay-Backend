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

    private final RestTemplate restTemplate = new RestTemplate();

    private String url = "http://localhost:8009";
    private String category = "movies";
    private String site = "1337x";

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private ObjectMapper mapper;



    private String getMoviesByRecent(){
        try {

            return restTemplate.getForObject("%s/api/v1/trending?site=%s&limit=50&category=%s".formatted(url, site, category ), String.class);
        } catch (RestClientException e) {
            throw new RuntimeException(e);
        }

    }
    @Override
    public void run(String... args) throws Exception {

        TorrentDataDto result = mapper.readValue(getMoviesByRecent(), TorrentDataDto.class);
        List<TorrentMovieDto> movieList = result.getData();
        List<Movie> dbMovies = movieRepo.findAll();

        Set<String> dbHashes = dbMovies.stream() //convert the list into a set if the hash is not in the is added
                .map(Movie::getHash)
                .collect(Collectors.toSet());
        for (TorrentMovieDto movie : movieList) {
            if (dbHashes.add(movie.getHash())) {
                movieService.createTorrentMovie(movie);
            }
        }

    }
}
