package org.sebas.magnetplay.repo;

import org.sebas.magnetplay.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepo extends JpaRepository<Movie, Long> {
}
