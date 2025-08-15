package org.sebas.magnetplay.repo;

import org.sebas.magnetplay.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepo extends JpaRepository<Users, Long> {
    Users findByUsername(String username);
}
