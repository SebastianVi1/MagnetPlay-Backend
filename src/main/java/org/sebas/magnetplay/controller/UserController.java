package org.sebas.magnetplay.controller;

import jakarta.validation.Valid;
import org.sebas.magnetplay.model.Users;
import org.sebas.magnetplay.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    UserService service;

    @Autowired
    public UserController(UserService service){
        this.service = service;
    }

    @PostMapping("/register")
    public Users registerNewUser(@RequestBody Users user) {
        return service.registerNewUser(user);

    }

    @PostMapping("/login")
    public String login(@RequestBody Users user){
        return service.verify(user);
    }

    @PostMapping("/register/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Users> logi(@RequestBody @Valid Users user){
        return service.registerNewAdminUser(user);
    }



    // Use http://localhost:8080/api/auth/login/oauth/code/google to use oauth2
}
