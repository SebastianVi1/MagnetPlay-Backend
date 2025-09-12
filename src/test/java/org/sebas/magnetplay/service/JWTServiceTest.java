package org.sebas.magnetplay.service;

import org.junit.jupiter.api.Test;
import org.sebas.magnetplay.model.Role;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.sebas.magnetplay.model.Users;
import org.sebas.magnetplay.repo.UsersRepo;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=bWlfY2xhdmVfc3VwZXJfc2VjcmV0YV9wYXJhX2p3dF8yMDI1"
})
public class JWTServiceTest {

    @Autowired
    private JWTService jwtService;

    @MockBean
    private UsersRepo usersRepo;

    @Test
    public void testTokenGenerationAndValidation() {

        String username = "testuser";

        // Mock user
        Users mockUser = new Users();
        mockUser.setId(1L);
        mockUser.setUsername(username);
        mockUser.setRoles(Set.of(new Role(null, "ROLE_USER")));
        mockUser.setEnabled(true);
        when(usersRepo.findByUsername(username)).thenReturn(mockUser);

        // Generate access token
        String accessToken = jwtService.generateToken(username);
        assertNotNull(accessToken);
        assertFalse(accessToken.isEmpty());

        // Generate refresh token
        String refreshToken = jwtService.generateRefreshToken(username);
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());

        // Extract username from access token
        String extractedUsername = jwtService.extractUsername(accessToken);
        assertEquals(username, extractedUsername);

        // Extract username from refresh token
        String extractedUsernameFromRefresh = jwtService.extractUsername(refreshToken);
        assertEquals(username, extractedUsernameFromRefresh);

        // Validate tokens
        assertTrue(jwtService.isTokenValid(accessToken));
        assertTrue(jwtService.isTokenValid(refreshToken));

        System.out.println("Access Token: " + accessToken);
        System.out.println("Refresh Token: " + refreshToken);
        System.out.println("Extracted username from access token: " + extractedUsername);
        System.out.println("Extracted username from refresh token: " + extractedUsernameFromRefresh);
    }
}
