package org.sebas.magnetplay.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.sebas.magnetplay.model.Users;
import org.sebas.magnetplay.repo.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {
    @Getter
    @Value("${jwt.secret}")
    private String secretKey;
    private UsersRepo usersRepo;

    private static final long ACCES_TOKEN_TTL_MS = 15 * 60 * 10L; //15 minutes
    private static final long REFRESH_TOKEN_TTL_MS = 7 * 24 * 60 * 60 * 1000L; // 7 days

    @Autowired
    public JWTService(UsersRepo usersRepo) {
        this.usersRepo = usersRepo;
        // secretKey is now injected from application.properties or environment
    }

    // Generate jwt token
    public String generateToken(String username){
        Map<String, Object> claims = new HashMap<>();
        /*
        Add info to the claims so the token can store data without db
        At this point we know the user is existent in the db
        */
        Users user= usersRepo.findByUsername(username);
        claims.put("userId", user.getId());
        claims.put("roles", user.getRoles());
        claims.put("isUserEnabled", user.isEnabled());

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCES_TOKEN_TTL_MS))
                .and()
                .signWith(getSigningKey())
                .compact();
    }

    // Generate refresh token
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        Users user = usersRepo.findByUsername(username);
        claims.put("userId", user.getId());
        claims.put("isUserEnabled", user.isEnabled());
        
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_TTL_MS))
                .and()
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver){
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build().parseSignedClaims(token)
                .getPayload();
    }

    public Boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date exp = claims.getExpiration();
            return !isTokenExpired(token) && exp.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            Claims claims = extractAllClaims(token);
            String subject = claims.getSubject();
            return subject != null
                    && subject.equals(userDetails.getUsername())
                    && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }
}
