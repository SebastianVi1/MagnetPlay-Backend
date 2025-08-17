package org.sebas.magnetplay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sebas.magnetplay.dto.UserDto;
import org.sebas.magnetplay.mapper.UserMapper;
import org.sebas.magnetplay.model.Role;
import org.sebas.magnetplay.model.Users;
import org.sebas.magnetplay.repo.RoleRepo;
import org.sebas.magnetplay.repo.UsersRepo;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UsersRepo userRepo;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleRepo roleRepo;

    private Users testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUserDto = new UserDto();
        testUserDto.setUsername("testName");
        testUserDto.setPassword("password");
        testUserDto.setEmail("email22@example.com");

        testUser = new Users();
        testUser.setUsername("testName");
        testUser.setPassword("password");
        testUser.setEmail("email22@example.com");

        // Emulate the behavior of the mocks
        when(userRepo.save(any(Users.class))).thenReturn(testUser);
        when(userMapper.toModel(any(UserDto.class))).thenReturn(testUser);
        when(userMapper.toDto(any(Users.class))).thenReturn(testUserDto);
        when(roleRepo.findByName(any(String.class))).thenReturn(
                Optional.of(
                        new Role(null, "ROLE_USER")
                )
        );
    }

    @Test
    void shouldRegisterANewUser(){
        // When
        var result = userService.registerNewUser(testUserDto);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(userRepo).save(any(Users.class));
        verify(roleRepo).findByName(any(String.class));
    }


}
