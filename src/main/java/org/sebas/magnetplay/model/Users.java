package org.sebas.magnetplay.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Long id;

    @NotBlank
    @Column(name = "username", unique = true)

    private String username;

    @Email
    private String email;

    @NotBlank
    @Column(name = "password")
    private String password;

    private boolean isEnabled = true;

    @ManyToMany(fetch = FetchType.EAGER) // The data is loaded with the entity
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @ManyToMany()
    @JoinTable(
            name = "user_favorites",
            joinColumns =  @JoinColumn(name = "user_id"),
            inverseJoinColumns =  @JoinColumn(name = "movie_id")
    )
    private Set<Movie> favoriteMovies = new HashSet<>();


}
