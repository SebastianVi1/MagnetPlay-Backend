package org.sebas.magnetplay.repo;

import org.sebas.magnetplay.model.MovieModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepo extends JpaRepository<MovieModel, Long> {
}
