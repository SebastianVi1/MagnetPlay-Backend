package org.sebas.magnetplay.service;

import org.sebas.magnetplay.model.UserPrincipal;
import org.sebas.magnetplay.model.Users;
import org.sebas.magnetplay.repo.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

import static org.hibernate.internal.CoreLogging.logger;

@Component
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    UsersRepo repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Looking for username: " + username);
        Users user =  repo.findByUsername(username); // get the user from db

        if (user == null){
            System.out.println("user not found");
            throw new UsernameNotFoundException("User not found");
        }



        return new UserPrincipal(user);


    }
}
