package org.sebas.magnetplay.repo;

import org.sebas.magnetplay.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovieRepo extends JpaRepository<Movie, Long> {
    Optional<Movie> findByHash(String hash);
    Optional<Movie> findByName(String name);
}
