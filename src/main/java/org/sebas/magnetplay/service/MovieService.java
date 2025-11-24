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
    private final MovieMapper movieMapper;
    private final MovieRepo repo;


    private final String url = "http://torrent-api:8009";
    private final String category = "movies";
    private final String site = "1337x";
    private final RestTemplate restTemplate;


    @Autowired
    public MovieService(MovieRepo repo, MovieMapper movieMapper, ObjectMapper objectMapper){
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
        return new ResponseEntity<>(movieMapper.toDto(movie.get()), HttpStatus.OK);
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

    public ResponseEntity<List<MovieDto>> getTrendingMovies() {
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
        TorrentDataDto dataDto = objectMapper.readValue(result, TorrentDataDto.class);
        List<TorrentMovieDto> torrents = dataDto != null && dataDto.getData() != null ? dataDto.getData() : Collections.emptyList();

        // Simple counters for quick troubleshooting
        int total = torrents.size();
        int skippedNoMagnet = 0;
        int skippedNoTitle = 0;

        Map<String, TorrentMovieDto> bestByTitle = new LinkedHashMap<>();

        Pattern res1080 = Pattern.compile("1080p", Pattern.CASE_INSENSITIVE);

        for (TorrentMovieDto t : torrents) {
            if (t == null) continue;

            // ensure magnet exists (or is present in url)
            if ((t.getMagnet() == null || t.getMagnet().trim().isEmpty()) && (t.getUrl() == null || !t.getUrl().toLowerCase().contains("magnet:"))) {
                skippedNoMagnet++;
                continue;
            }
            if ((t.getMagnet() == null || t.getMagnet().trim().isEmpty()) && t.getUrl() != null && t.getUrl().toLowerCase().contains("magnet:")) {
                t.setMagnet(t.getUrl());
            }

            // build candidate title
            String candidate = t.getName();
            if (candidate == null || candidate.trim().isEmpty()) {
                if (t.getUrl() != null && !t.getUrl().trim().isEmpty()) {
                    String url = t.getUrl();
                    int idx = Math.max(url.lastIndexOf('/'), url.lastIndexOf('='));
                    candidate = idx > -1 && idx + 1 < url.length() ? url.substring(idx + 1) : url;
                }
            }
            if ((candidate == null || candidate.trim().isEmpty()) && t.getDescription() != null) {
                candidate = t.getDescription();
            }

            // REQUIRE: only keep torrents that are 1080p (check before parsing)
            if (candidate == null || !res1080.matcher(candidate).find()) {
                skippedNoTitle++;
                continue;
            }

            String parsed = parseMovieTitle(candidate);
            if (parsed == null || parsed.trim().isEmpty()) {
                skippedNoTitle++;
                continue;
            }

            String normalized = parsed.trim().toLowerCase().replaceAll("\\s+", " ");
            t.setName(parsed.trim());

            // fallback: if poster missing, use first screenshot available
            if ((t.getPoster() == null || t.getPoster().trim().isEmpty()) && t.getScreenshot() != null && !t.getScreenshot().isEmpty()) {
                t.setPoster(t.getScreenshot().get(0));
            }

            TorrentMovieDto current = bestByTitle.get(normalized);
            if (current == null || calculateTorrentQualityScore(t) > calculateTorrentQualityScore(current)) {
                bestByTitle.put(normalized, t);
            }
        }

        System.out.println("saveTorrentInDatabase: total=" + total + ", kept=" + bestByTitle.size() + ", skippedNoMagnet=" + skippedNoMagnet + ", skippedNoTitle=" + skippedNoTitle);

        List<Movie> savedMovies = new ArrayList<>();
        for (TorrentMovieDto t : bestByTitle.values()) {
            try {
                String name = t.getName();
                Optional<Movie> byName = repo.findFirstByNameIgnoreCase(name);
                Optional<Movie> byHash = t.getHash() != null ? repo.findByHash(t.getHash()) : Optional.empty();

                if (byName.isPresent()) {
                    savedMovies.add(byName.get());
                } else if (byHash.isPresent()) {
                    savedMovies.add(byHash.get());
                } else {
                    Movie toSave = movieMapper.fromTorrentToMovie(t);
                    Movie saved = repo.save(toSave);
                    savedMovies.add(saved);
                }
            } catch (Exception e) {
                System.err.println("Error saving movie: " + (t != null ? t.getName() : "unknown") + " -> " + e.getMessage());
            }
        }

        return movieMapper.toDtoList(savedMovies);
    }

    /**
     * Calculate quality score based on audio/subtitle availability and poster
     */
    private int calculateTorrentQualityScore(TorrentMovieDto torrent) {
        int score = 0;
        String name = torrent.getName() != null ? torrent.getName().toLowerCase() : "";
        String filesContent = "";

        if (torrent.getFiles() != null && !torrent.getFiles().isEmpty()) {
            filesContent = String.join(" ", torrent.getFiles()).toLowerCase();
        }

        String searchText = name + " " + filesContent;

        // Poster (highest priority)
        if (torrent.getPoster() != null && !torrent.getPoster().trim().isEmpty()) {
            score += 20;
        }

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

    public String parseMovieTitle(String title){
        if (title == null) return "";
        ParseMovie info = new ParseMovie();

        // Remove parentheses and common special characters
        String cleanTitle = title.replaceAll("[()\\[\\]{}]", " ")
                                 .replaceAll("[.]", " ")
                                 .replaceAll("_", " ")
                                 .replaceAll("\\s+", " ")
                                 .trim();

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

        // Return ONLY name and year (no resolution to avoid duplicates)
        if (info.year.isEmpty()) {
            return info.name;
        }
        return String.format("%s %s", info.name, info.year).replaceAll("\\s+", " ").trim();
    }


    public ResponseEntity<?> searchMovie(String movieName) {
        try {
            System.out.println("getting: " + movieName);
            String result = restTemplate.getForObject("%s/api/v1/search?site=piratebay&query=%s&limit=20".formatted(url, movieName), String.class);
            List<MovieDto> finalResult = saveTorrentInDatabase(result);
            return new ResponseEntity<>(finalResult, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error on getTrendingMovies: " + e.getMessage());
            throw new RestClientException("Error obtaining the movie " + movieName, e);
        }
    }
}