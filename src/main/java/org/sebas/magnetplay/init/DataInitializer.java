package org.sebas.magnetplay.init;

import org.sebas.magnetplay.dto.MovieDto;
import org.sebas.magnetplay.dto.UserDto;
import org.sebas.magnetplay.model.Movie;
import org.sebas.magnetplay.model.MovieCategory;
import org.sebas.magnetplay.model.Role;
import org.sebas.magnetplay.model.Users;
import org.sebas.magnetplay.repo.MovieRepo;
import org.sebas.magnetplay.repo.RoleRepo;
import org.sebas.magnetplay.repo.UsersRepo;
import org.sebas.magnetplay.service.MovieService;
import org.sebas.magnetplay.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    //Create the roles in the database if not exists

    @Autowired
    private RoleRepo roleRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UsersRepo userRepo;

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private MovieService movieService;


    @Override
    public void run(String... args) throws Exception {
        MovieDto testMovie = new MovieDto();
        testMovie.setName("Avengers");
        testMovie.setDescription("Test description...");
        testMovie.setCategories(List.of(
                MovieCategory.ACTION,
                MovieCategory.COMEDY
        ));
        testMovie.setImageUri("https://wallpapercave.com/wp/wp4770368.jpg");
        var result = movieService.createMovie(testMovie);
        System.out.println(testMovie.getCategories());
        System.out.println("Movie added");

        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        for (String roleName : roles) {
            roleRepository.findByName(roleName)
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName(roleName);
                        return roleRepository.save(role);
                    });
        }

        System.out.println("Roles initialized");


        if(userRepo.findAll().isEmpty()){
            UserDto admin = new UserDto();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            userService.registerNewAdminUser(admin);
            System.out.println("Default admin user added, use admin and admin123 as password");
        }
    }
}
