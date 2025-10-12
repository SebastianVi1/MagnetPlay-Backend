package org.sebas.magnetplay.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sebas.magnetplay.dto.MovieDto;
import org.sebas.magnetplay.dto.TorrentDataDto;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class MovieService {

    private final ObjectMapper objectMapper;
    private MovieMapper movieMapper;
    private MovieRepo repo;


    private final String url = "http://torrent-api:8009";
    private final String category = "movies";
    private final String site = "1337x";
    private final RestTemplate restTemplate;


    @Autowired
    public MovieService(MovieRepo repo, MovieMapper movieMapper, UsersRepo usersRepo, ObjectMapper objectMapper){
        this.repo = repo;
        this.movieMapper = movieMapper;
        this.objectMapper = objectMapper;

        restTemplate = new RestTemplate();
    }

    public ResponseEntity<List<MovieDto>> getMovies(){
        List<Movie> movieList = repo.findAll();
        List<MovieDto> movieDtos = movieMapper.toDtoList(movieList);
        return new ResponseEntity<>(movieDtos, HttpStatus.OK);

    }


    public ResponseEntity<MovieDto> getMovieById(Long id) {
        Optional<Movie> movie = repo.findById(id);
        if (movie.isEmpty()){
            throw new MovieNotFoundException("Movie with the id: %d not found".formatted(id));
        }
        return new ResponseEntity<MovieDto>(movieMapper.toDto(movie.get()), HttpStatus.OK);
    }

    public ResponseEntity<MovieDto> createMovie(MovieDto movieDto) throws InvalidDataException {
        if (movieDto == null){
            throw new InvalidDataException("Movie cannot be null");
        }

        Movie movie = repo.save(movieMapper.toModel(movieDto));

        return new ResponseEntity<>(movieMapper.toDto(movie), HttpStatus.CREATED);
    }

    public ResponseEntity<?> updateMovie(Long movieId, MovieDto updatedMovie){
        if (repo.findById(movieId).isEmpty()){
            throw new MovieNotFoundException("Movie with the id: %d not found".formatted(movieId));
        }
        MovieDto result = movieMapper.toDto(
                repo.save(movieMapper.toModel(updatedMovie))
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


    public ResponseEntity<?> createTorrentMovie(TorrentMovieDto torrentMovie){
        String parsedTitle = parseMovieTitle(torrentMovie.getName());
        torrentMovie.setName(parsedTitle);
        if (!is1080pTorrent(torrentMovie)){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Movie movieEntity = movieMapper.fromTorrentToMovie(torrentMovie);
        Movie newMovie = repo.save(movieEntity);
        return new ResponseEntity<>(newMovie, HttpStatus.CREATED);

    }


    public ResponseEntity<List<MovieDto>> getRecentMovies() throws JsonProcessingException {
        try {
            String result = restTemplate.getForObject("%s/api/v1/recent?site=%s&limit=100&category=%s".formatted(url, site, category ), String.class);
            List<MovieDto> finalResult = saveTorrentInDatabase(result);
            return new ResponseEntity<>(finalResult, HttpStatus.OK);
        } catch (RestClientException e) {
            System.err.println("Error on getRecentMovies: " + e.getMessage());
            throw new RestClientException("Error getting the recent movies", e);
        }
    }

    public ResponseEntity<List<MovieDto>> getTrendingMovies() throws JsonProcessingException {
        try {
            String result = restTemplate.getForObject("%s/api/v1/trending?site=%s&limit=100&category=%s".formatted(url, site, category ), String.class);
            List<MovieDto> finalResult = saveTorrentInDatabase(result);
            return new ResponseEntity<>(finalResult, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error on getTrendingMovies: " + e.getMessage());
            throw new RestClientException("Error obtaining the movies", e);
        }
    }

    public List<MovieDto> saveTorrentInDatabase(String result) throws JsonProcessingException {
        TorrentDataDto resultDto = objectMapper.readValue(result, TorrentDataDto.class);
        List<TorrentMovieDto> torrentMovieDtos = resultDto.getData();

        // Step 1: Filter and clean torrents (only 1080p with valid titles)
        Map<String, TorrentMovieDto> uniqueTorrents = new LinkedHashMap<>();

        for (TorrentMovieDto torrent : torrentMovieDtos) {
            // Skip if not 1080p
            if (!is1080pTorrent(torrent)) {
                continue;
            }

            // Parse and clean title
            String parsedTitle = parseMovieTitle(torrent.getName());
            if (parsedTitle == null || parsedTitle.trim().isEmpty()) {
                continue;
            }

            parsedTitle = parsedTitle.trim();
            torrent.setName(parsedTitle);

            // Keep only one torrent per title (best quality)
            if (!uniqueTorrents.containsKey(parsedTitle)) {
                uniqueTorrents.put(parsedTitle, torrent);
            } else {
                // Compare and keep better quality
                TorrentMovieDto existing = uniqueTorrents.get(parsedTitle);
                int newScore = calculateTorrentQualityScore(torrent);
                int existingScore = calculateTorrentQualityScore(existing);

                if (newScore > existingScore) {
                    uniqueTorrents.put(parsedTitle, torrent);
                } else if (newScore == existingScore && hasBetterPoster(torrent, existing)) {
                    uniqueTorrents.put(parsedTitle, torrent);
                }
            }
        }

        // Step 2: Save to database
        List<Movie> savedMovies = new ArrayList<>();
        int newMovies = 0;
        int existingMovies = 0;

        for (TorrentMovieDto torrent : uniqueTorrents.values()) {
            try {
                // Check if already exists in DB
                Optional<Movie> existingMovie = repo.findFirstByNameIgnoreCase(torrent.getName());

                if (existingMovie.isPresent()) {
                    savedMovies.add(existingMovie.get());
                    existingMovies++;
                } else {
                    // Save new movie
                    Movie newMovie = movieMapper.fromTorrentToMovie(torrent);
                    Movie saved = repo.save(newMovie);
                    savedMovies.add(saved);
                    newMovies++;
                }
            } catch (Exception e) {
                System.err.println("❌ Error saving movie: " + torrent.getName() + " - " + e.getMessage());
            }
        }

        return movieMapper.toDtoList(savedMovies);
    }

    /**
     * Check if torrent has a better poster
     */
    private boolean hasBetterPoster(TorrentMovieDto newTorrent, TorrentMovieDto existing) {
        boolean newHasPoster = newTorrent.getPoster() != null && !newTorrent.getPoster().trim().isEmpty();
        boolean existingHasPoster = existing.getPoster() != null && !existing.getPoster().trim().isEmpty();
        return newHasPoster && !existingHasPoster;
    }

    /**
     * Calculate quality score based on audio/subtitle availability
     */
    private int calculateTorrentQualityScore(TorrentMovieDto torrent) {
        int score = 0;
        String name = torrent.getName() != null ? torrent.getName().toLowerCase() : "";
        String filesContent = "";

        if (torrent.getFiles() != null && !torrent.getFiles().isEmpty()) {
            filesContent = String.join(" ", torrent.getFiles()).toLowerCase();
        }

        String searchText = name + " " + filesContent;

        // Audio codecs/formats
        if (searchText.contains(".ac3") || searchText.contains(".aac") ||
            searchText.contains(".dts") || searchText.contains("dolby") ||
            searchText.contains("atmos") || searchText.contains("5.1") ||
            searchText.contains("7.1") || searchText.contains("truehd")) {
            score += 15;
        }

        // English audio
        if (searchText.contains("english") || searchText.contains("eng")) {
            score += 10;
        }

        // Subtitles
        if (searchText.contains(".srt") || searchText.contains(".sub") ||
            searchText.contains(".ass") || searchText.contains("subtitle")) {
            score += 8;
        }

        // Seeders (popularity)
        if (torrent.getSeeders() != null && !torrent.getSeeders().isEmpty()) {
            try {
                int seeders = Integer.parseInt(torrent.getSeeders().replaceAll("[^0-9]", ""));
                score += Math.min(seeders / 10, 5);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        return score;
    }

    public ResponseEntity<Map<String,List<MovieDto>>> getOrderedByCategory(){
        List<MovieDto> movieList = movieMapper.toDtoList(repo.findAll());

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

        // Remove parentheses and common special characters
        String cleanTitle = title.replaceAll("[()\\[\\]{}]", " ").replaceAll("[.]", " ").replaceAll("_", " ").replaceAll("\\s+", " ").trim();

        // Extract year
        Pattern yearPattern = Pattern.compile("(19|20)\\d{2}");
        Matcher yearMatcher = yearPattern.matcher(cleanTitle);
        if (yearMatcher.find()) {
            info.year = yearMatcher.group();
            info.name = cleanTitle.substring(0, yearMatcher.start()).trim();
        } else {
            info.name = cleanTitle.trim();
            info.year = "";
        }
        Pattern resPattern = Pattern.compile("(2160p|1080p|720p|480p)");
        Matcher resMatcher = resPattern.matcher(cleanTitle);
        if (resMatcher.find()) {
            info.resolution = resMatcher.group();
        } else {
            info.resolution = "";
        }
        // Unify format for comparison
        return String.format("%s %s %s", info.name, info.year, info.resolution).replaceAll("\\s+", " ").trim();
    }


    public boolean is1080pTorrent(TorrentMovieDto torrentMovieDto) {
        if (torrentMovieDto == null || torrentMovieDto.getName() == null) return false;
        String title = torrentMovieDto.getName();
        Pattern resPattern = Pattern.compile("1080p");
        Matcher matcher = resPattern.matcher(title);
        return matcher.find();
    }
}
