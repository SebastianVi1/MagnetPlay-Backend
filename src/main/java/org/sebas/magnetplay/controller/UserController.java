package org.sebas.magnetplay.controller;

import jakarta.validation.Valid;
import org.sebas.magnetplay.dto.AuthResponseDto;
import org.sebas.magnetplay.dto.UserDto;
import org.sebas.magnetplay.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

   private UserService service;

    @Autowired
    public UserController(UserService service){
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registerNewUser(@RequestBody UserDto user) {
        return service.registerNewUser(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody UserDto user){
        return service.verifyUser(user);
    }

    @PostMapping("/register/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponseDto> registerNewAdminUser(@RequestBody @Valid UserDto user){
        return service.registerNewAdminUser(user);
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> post(@RequestBody String value) {
        return service.isTokenValid(value);
    }



    // Use http://localhost:8080/api/auth/login/oauth/code/google to use oauth2
}
