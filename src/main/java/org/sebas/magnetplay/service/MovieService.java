package org.sebas.magnetplay.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sebas.magnetplay.dto.MovieDto;
import org.sebas.magnetplay.dto.TorrentMovieDto;
import org.sebas.magnetplay.exceptions.InvalidDataException;
import org.sebas.magnetplay.exceptions.MovieNotFoundException;
import org.sebas.magnetplay.mapper.MovieMapper;
import org.sebas.magnetplay.model.Movie;
import org.sebas.magnetplay.model.ParseMovie;
import org.sebas.magnetplay.repo.MovieRepo;
import org.sebas.magnetplay.repo.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.awt.SystemColor.info;


@Service
public class MovieService {

    private final UsersRepo usersRepo;
    private final ObjectMapper objectMapper;
    private MovieMapper mapper;
    private MovieRepo repo;


    private String url = "http://torrent-api:8009";
    private String category = "movies";
    private String site = "1337x";
    private RestTemplate restTemplate;


    @Autowired
    public MovieService(MovieRepo repo, MovieMapper mapper, UsersRepo usersRepo, ObjectMapper objectMapper){
        this.repo = repo;
        this.mapper = mapper;
        this.usersRepo = usersRepo;
        this.objectMapper = objectMapper;

        restTemplate = new RestTemplate();
    }

    public ResponseEntity<List<MovieDto>> getMovies(){
        List<Movie> movieList = repo.findAll();
        List<MovieDto> movieDtos = new ArrayList<MovieDto>();

        movieDtos = movieList.stream()
                .map((model) -> mapper.toDto(model))
                .toList();
        return new ResponseEntity<List<MovieDto>>(movieDtos, HttpStatus.OK);

    }


    public ResponseEntity<MovieDto> getMovieById(Long id) {
        Optional<Movie> movie = repo.findById(id);
        if (movie.isEmpty()){
            throw new MovieNotFoundException("Movie with the id: %d not found".formatted(id));
        }
        return new ResponseEntity<MovieDto>(mapper.toDto(movie.get()), HttpStatus.OK);
    }

    public ResponseEntity<MovieDto> createMovie(MovieDto movieDto) throws InvalidDataException {
        if (movieDto == null){
            throw new InvalidDataException("Movie cannot be null");
        }

        Movie movie = repo.save(mapper.toModel(movieDto));

        return new ResponseEntity<>(mapper.toDto(movie), HttpStatus.CREATED);
    }

    public ResponseEntity<?> updateMovie(Long movieId, MovieDto updatedMovie){
        if (repo.findById(movieId).isEmpty()){
            throw new MovieNotFoundException("Movie with the id: %d not found".formatted(movieId));
        }
        MovieDto result = mapper.toDto(
                repo.save(mapper.toModel(updatedMovie))
        );

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteMovie(Long id) throws MovieNotFoundException {
        Optional<Movie> movieOptional = repo.findById(id);
        if (movieOptional.isEmpty()){
            throw new MovieNotFoundException("Movie with the id: %d not found".formatted(id));
        }
        Movie movie = movieOptional.get();
        repo.delete(movie);

        return new ResponseEntity<>("Movie %s deleted".formatted(movie.getName()), HttpStatus.OK);
    }


    public ResponseEntity<?> createTorrentMovie(@RequestBody TorrentMovieDto torrentMovie){
        String parsedTitle = parseMovieTitle(torrentMovie.getName());
        torrentMovie.setName(parsedTitle);
        if (!is1080pTorrent(torrentMovie)){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Movie movieEntity = mapper.fromTorrentToMovie(torrentMovie);
        var newMovie = repo.save(movieEntity);
        return new ResponseEntity<>(newMovie, HttpStatus.CREATED);

    }


    public ResponseEntity<String> getRecentMovies(){

        try {
             String result = restTemplate.getForObject("%s/api/v1/recent?site=%s&limit=200&category=%s".formatted(url, site, category ), String.class);
             return new ResponseEntity<String>(result, HttpStatus.OK);
        } catch (RestClientException e) {
            System.out.println("error");
            throw new RestClientException(e.getMessage());
        }

    }

    public ResponseEntity<String> getTrendingMovies(){
        // TODO: Add pages

        try {
            String result = restTemplate.getForObject("%s/api/v1/trending?site=%s&limit=200&category=%s".formatted(url, site, category ), String.class);
            return new ResponseEntity<String>(result, HttpStatus.OK);
        } catch (RestClientException e) {
            System.out.println("error");
            throw new RestClientException(e.getMessage());
        }

    }

    public ResponseEntity<Map<String,List<MovieDto>>> getOrderedByCategory(){
        List<MovieDto> movieList = mapper.toDtoList(repo.findAll());

        HashSet<String> categories = new HashSet<>();
        HashMap<String, List<MovieDto>> categoryMap = new HashMap<>();

        for (MovieDto movie : movieList){
            if (movie.getCategory() == null){
                System.out.println(movie.getCategory());
                continue;
            }
            categories.add(movie.getCategory());
        }
        for (String category: categories){
            categoryMap.put(category, new ArrayList<>());
        }
       for (MovieDto movie : movieList){
           for (String category : categoryMap.keySet()){
               if( category.equals(movie.getCategory())){
                   categoryMap.get(category).add(movie);
               }
           }
       }
       return new ResponseEntity<>(categoryMap, HttpStatus.OK);
    }

    public String parseMovieTitle(String title){
        ParseMovie info = new ParseMovie();
        Pattern yearPattern = Pattern.compile("(19|20)\\d{2}");
        Matcher yearMatcher = yearPattern.matcher(title);
        if (yearMatcher.find()) {
            info.year = yearMatcher.group();
            info.name = title.substring(0, yearMatcher.start()).replaceAll("[.]", " ").trim();
        }
        Pattern resPattern = Pattern.compile("(2160p|1080p|720p|480p)");
        Matcher resMatcher = resPattern.matcher(title);
        if (resMatcher.find()) {
            info.resolution = resMatcher.group();
        }
        return String.format("%s %s %s", info.name, info.year, info.resolution);
    }



    public boolean is1080pTorrent(TorrentMovieDto torrentMovieDto) {
        if (torrentMovieDto == null || torrentMovieDto.getName() == null) return false;
        String title = torrentMovieDto.getName();
        Pattern resPattern = Pattern.compile("1080p");
        Matcher matcher = resPattern.matcher(title);
        return matcher.find();
    }


    public ResponseEntity<?> streamMovie(Long id) {
        return null;
    }
}
