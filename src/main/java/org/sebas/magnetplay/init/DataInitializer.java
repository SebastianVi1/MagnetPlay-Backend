package org.sebas.magnetplay.init;

import org.sebas.magnetplay.model.Role;
import org.sebas.magnetplay.repo.RoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    //Create the roles in the database if not exists

    @Autowired
    private RoleRepo roleRepository;

    @Override
    public void run(String... args) throws Exception {

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
    }
}
