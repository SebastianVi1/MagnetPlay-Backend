package org.sebas.magnetplay.service;

import org.sebas.magnetplay.model.Users;
import org.sebas.magnetplay.repo.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    UsersRepo repo;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Autowired
    public UserService(UsersRepo repo){
        this.repo = repo;
    }

    public Users registerNewUser(Users user){

        user.setPassword(encoder.encode(user.getPassword()));
        return repo.save(user);

    }
}
