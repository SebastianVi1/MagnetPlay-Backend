package org.sebas.magnetplay.repo;

import org.sebas.magnetplay.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepo extends JpaRepository<Movie, Long> {
    List<Movie> findByCategories(List<String> categories);
}
