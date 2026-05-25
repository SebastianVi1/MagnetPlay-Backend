package org.sebas.magnetplay.service;

import org.sebas.magnetplay.dto.tmdb.TmdbMovieDetails;
import org.sebas.magnetplay.dto.tmdb.TmdbMovieResult;
import org.sebas.magnetplay.dto.tmdb.TmdbSearchResponse;
import org.sebas.magnetplay.model.Movie;
import org.sebas.magnetplay.repo.MovieRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TmdbService {

    @Value("${app.tmdb.api-key:}")
    private String tmdbApiKey;

    @Value("${app.tmdb.url}")
    private String tmdbUrl;

    @Value("${app.tmdb.image-base-url}")
    private String imageBaseUrl;

    private final MovieRepo movieRepo;

    public TmdbService(MovieRepo movieRepo) {
        this.movieRepo = movieRepo;
    }

    private boolean isConfigured() {
        return tmdbApiKey != null && !tmdbApiKey.isBlank();
    }

    public String buildPosterUrl(String posterPath) {
        if (posterPath == null || posterPath.isBlank()) return null;
        return imageBaseUrl + "/w500" + posterPath;
    }

    public String buildBackdropUrl(String backdropPath) {
        if (backdropPath == null || backdropPath.isBlank()) return null;
        return imageBaseUrl + "/original" + backdropPath;
    }

    public Optional<TmdbMovieResult> searchMovie(String name, String year) {
        if (!isConfigured()) return Optional.empty();

        try {
            RestTemplate rt = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(tmdbApiKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String query = name.replaceAll("\\s+", " ").trim();
            String url = tmdbUrl + "/search/movie?query="
                    + java.net.URLEncoder.encode(query, "UTF-8")
                    + "&language=en-US";

            if (year != null && !year.isEmpty()) {
                url += "&year=" + year;
            }

            ResponseEntity<TmdbSearchResponse> response =
                    rt.exchange(url, HttpMethod.GET, entity, TmdbSearchResponse.class);

            TmdbSearchResponse body = response.getBody();
            if (body == null || body.getResults() == null || body.getResults().isEmpty()) {
                return Optional.empty();
            }

            return pickBestMatch(body.getResults(), name, year);
        } catch (Exception e) {
            System.err.println("TMDB search error for '" + name + "': " + e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<TmdbMovieResult> pickBestMatch(List<TmdbMovieResult> results, String name, String year) {
        if (results.isEmpty()) return Optional.empty();

        if (year != null && !year.isEmpty()) {
            for (TmdbMovieResult r : results) {
                if (r.getReleaseDate() != null && r.getReleaseDate().startsWith(year)) {
                    return Optional.of(r);
                }
            }
        }

        if (results.get(0).getVoteAverage() > 0 || results.get(0).getPosterPath() != null) {
            return Optional.of(results.get(0));
        }

        return Optional.of(results.get(0));
    }

    public Optional<TmdbMovieDetails> getMovieDetails(int tmdbId) {
        if (!isConfigured()) return Optional.empty();

        try {
            RestTemplate rt = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(tmdbApiKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = tmdbUrl + "/movie/" + tmdbId + "?language=en-US";
            ResponseEntity<TmdbMovieDetails> response =
                    rt.exchange(url, HttpMethod.GET, entity, TmdbMovieDetails.class);

            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            System.err.println("TMDB details error for id " + tmdbId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    public void enrichMovie(Movie movie) {
        if (!isConfigured()) return;
        if (movie.getTmdbId() != null) return;

        ParsedTitle parsed = parseTitle(movie.getName());
        if (parsed.name == null || parsed.name.isBlank()) return;

        Optional<TmdbMovieResult> resultOpt = searchMovie(parsed.name, parsed.year);
        if (resultOpt.isEmpty()) {
            if (parsed.year != null && !parsed.year.isEmpty()) {
                resultOpt = searchMovie(parsed.name, null);
            }
            if (resultOpt.isEmpty()) return;
        }

        TmdbMovieResult result = resultOpt.get();
        applySearchResult(movie, result);

        Optional<TmdbMovieDetails> detailsOpt = getMovieDetails(result.getId());
        if (detailsOpt.isPresent()) {
            TmdbMovieDetails details = detailsOpt.get();
            applyDetails(movie, details);
        }

        try {
            movieRepo.save(movie);
        } catch (Exception e) {
            System.err.println("TMDB save error for movie " + movie.getId() + ": " + e.getMessage());
        }
    }

    private void applySearchResult(Movie movie, TmdbMovieResult result) {
        movie.setTmdbId(result.getId());
        movie.setTmdbPosterPath(result.getPosterPath());
        movie.setTmdbBackdropPath(result.getBackdropPath());
        movie.setTmdbRating(result.getVoteAverage());
        movie.setTmdbOverview(result.getOverview());
        movie.setReleaseDate(result.getReleaseDate());
    }

    private void applyDetails(Movie movie, TmdbMovieDetails details) {
        movie.setRuntime(details.getRuntime());

        if (details.getOverview() != null && !details.getOverview().isBlank()) {
            movie.setTmdbOverview(details.getOverview());
        }
        if (details.getPosterPath() != null && !details.getPosterPath().isBlank()) {
            movie.setTmdbPosterPath(details.getPosterPath());
        }
        if (details.getBackdropPath() != null && !details.getBackdropPath().isBlank()) {
            movie.setTmdbBackdropPath(details.getBackdropPath());
        }
        if (details.getVoteAverage() > 0) {
            movie.setTmdbRating(details.getVoteAverage());
        }
        if (details.getReleaseDate() != null && !details.getReleaseDate().isBlank()) {
            movie.setReleaseDate(details.getReleaseDate());
        }
        if (details.getGenres() != null && !details.getGenres().isEmpty()
                && (movie.getGenres() == null || movie.getGenres().isEmpty())) {
            List<String> genreNames = details.getGenres().stream()
                    .map(g -> g.getName())
                    .toList();
            movie.setGenres(genreNames);
        }
    }

    static class ParsedTitle {
        String name;
        String year;
    }

    static ParsedTitle parseTitle(String title) {
        ParsedTitle result = new ParsedTitle();
        if (title == null || title.isBlank()) {
            result.name = "";
            result.year = null;
            return result;
        }

        String cleanTitle = title.replaceAll("[()\\[\\]{}]", " ")
                .replaceAll("[.]", " ")
                .replaceAll("_", " ")
                .replaceAll("\\s+", " ")
                .trim();

        Pattern yearPattern = Pattern.compile("(19|20)\\d{2}");
        Matcher yearMatcher = yearPattern.matcher(cleanTitle);
        if (yearMatcher.find()) {
            result.year = yearMatcher.group();
            result.name = cleanTitle.substring(0, yearMatcher.start()).trim();
        } else {
            result.name = cleanTitle.trim();
            result.year = null;
        }

        return result;
    }

    public int backfillAll() {
        if (!isConfigured()) return 0;

        List<Movie> movies = movieRepo.findByTmdbIdIsNull();
        int updated = 0;
        for (Movie movie : movies) {
            try {
                enrichMovie(movie);
                updated++;
                Thread.sleep(250);
            } catch (Exception e) {
                System.err.println("Backfill error: " + e.getMessage());
            }
        }
        return updated;
    }
}
