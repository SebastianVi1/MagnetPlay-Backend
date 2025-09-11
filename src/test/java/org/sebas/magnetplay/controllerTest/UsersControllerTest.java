package org.sebas.magnetplay.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.sebas.magnetplay.controller.UserController;
import org.sebas.magnetplay.dto.AuthResponseDto;
import org.sebas.magnetplay.dto.UserDto;
import org.sebas.magnetplay.mapper.UserMapper;
import org.sebas.magnetplay.model.Users;
import org.sebas.magnetplay.service.JWTService;
import org.sebas.magnetplay.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UsersControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JWTService jwtService;


    @Test
    void shouldCreateAUserWithStatusOK200() throws Exception {
        UserDto user = new UserDto();
        user.setUsername("testUser");
        user.setPassword("admin");

        AuthResponseDto response = new AuthResponseDto();
        response.setUser(user);
        response.setToken("mocked-jwt-token");

        when(userService.registerNewUser(any(UserDto.class))).thenReturn(ResponseEntity.of(Optional.of(response)));
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("testUser"))
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    @Test
    void shouldLoginUserAndReturnToken() throws Exception {
        UserDto user = new UserDto();
        user.setUsername("testUser");
        user.setPassword("admin");

        String token = "mocked-jwt-token";

        AuthResponseDto response = new AuthResponseDto();
        response.setUser(user);
        response.setToken(token);
        when(userService.verifyUser(any(UserDto.class))).thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequestForInvalidAdminUser() throws Exception {
        UserDto invalidAdminUser = new UserDto();
        // No username or password set, should fail validation

        mockMvc.perform(post("/api/auth/register/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidAdminUser)));
    }

    @Test
    void shouldReturnConflictWhenAdminUserAlreadyExists() throws Exception {
        UserDto adminUser = new UserDto();
        adminUser.setUsername("adminUser");
        adminUser.setPassword("adminPass");

        when(userService.registerNewAdminUser(any(UserDto.class)))
                .thenThrow(new org.sebas.magnetplay.exceptions.UsernameTakenException("Username already taken"));

        mockMvc.perform(post("/api/auth/register/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminUser)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already taken"));
    }

    @Test
    void shouldCreateAdminUserWithStatusOK200() throws Exception {
        UserDto adminUser = new UserDto();
        adminUser.setUsername("adminUser");
        adminUser.setPassword("adminPass");

        AuthResponseDto response = new AuthResponseDto();
        response.setUser(adminUser);
        response.setToken("mocked-admin-token");

        when(userService.registerNewAdminUser(any(UserDto.class)))
                .thenReturn(ResponseEntity.of(Optional.of(response)));

        mockMvc.perform(post("/api/auth/register/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value("adminUser"))
                .andExpect(jsonPath("$.token").value("mocked-admin-token"));
    }

}
