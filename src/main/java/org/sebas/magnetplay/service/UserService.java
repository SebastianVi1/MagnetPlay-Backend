package org.sebas.magnetplay.service;

import org.sebas.magnetplay.dto.AuthResponseDto;
import org.sebas.magnetplay.dto.UserDto;
import org.sebas.magnetplay.exceptions.UsernameTakenException;
import org.sebas.magnetplay.mapper.UserMapper;
import org.sebas.magnetplay.model.Role;
import org.sebas.magnetplay.model.Users;
import org.sebas.magnetplay.repo.RoleRepo;
import org.sebas.magnetplay.repo.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private JWTService jwtService;

    private RoleRepo roleRepo;

    private UserMapper userMapper;

    private UsersRepo usersRepo;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    AuthenticationManager authManager;

    @Autowired
    public UserService(UsersRepo usersRepo, AuthenticationManager authManager, JWTService jwtService, RoleRepo roleRepo, UserMapper userMapper){
        this.jwtService = jwtService;
        this.authManager = authManager;
        this.usersRepo = usersRepo;
        this.roleRepo = roleRepo;
        this.userMapper = userMapper;
    }


    public ResponseEntity<UserDto> registerNewUser(UserDto userDto){

        // Convert UserDto to Entity
        Users user = userMapper.toModel(userDto);

        // Verify if the username exists
        if (usersRepo.findByUsername(user.getUsername()) != null){
            throw new UsernameTakenException("The username: %s is already in use".formatted(user.getUsername()));
        }

        Role role = roleRepo.findByName("ROLE_USER").get(); //assign user rol by default
        user.setRoles(Set.of(role));
        user.setPassword(encoder.encode(user.getPassword())); // encrypt the password
        //save the new user
        user = usersRepo.save(user);
        userDto = userMapper.toDto(user);
        return new ResponseEntity<UserDto>(userDto, HttpStatus.CREATED);
    }

    public ResponseEntity<UserDto> registerNewAdminUser(UserDto userDto){
        Users user = userMapper.toModel(userDto);
        // Verify if the username exists
        if (usersRepo.findByUsername(user.getUsername()) != null){
            throw new UsernameTakenException("The username: %s is already in use".formatted(user.getUsername()));
        }
        List<Role> roles = roleRepo.findAll();
        user.setRoles(
                Set.of(
                        roles.get(0),
                        roles.get(1)
                )
        );
        user.setPassword(encoder.encode(user.getPassword())); // encrypt the password
        Users userSaved =  usersRepo.save(user);
        UserDto savedUserDto = userMapper.toDto(userSaved);
        return new ResponseEntity<UserDto>(savedUserDto, HttpStatus.CREATED);
    }

    public ResponseEntity<AuthResponseDto> verifyUser(UserDto user) {
        Authentication authentication =
                authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

        if (authentication.isAuthenticated()){
            String token =  jwtService.generateToken(user.getUsername());
            UserDto dbUser = userMapper.toDto(usersRepo.findByUsername(user.getUsername()));
            AuthResponseDto response = new AuthResponseDto();
            response.setUser(dbUser);
            response.setToken(token);


            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        throw new BadCredentialsException("The autentication failed");
    }
}
