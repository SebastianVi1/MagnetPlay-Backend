package org.sebas.magnetplay.controller;

import org.sebas.magnetplay.model.Users;
import org.sebas.magnetplay.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
}
