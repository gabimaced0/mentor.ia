package com.reskill.service.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.reskill.dto.auth.TokenResponse;
import com.reskill.model.User;
import com.reskill.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class TokenService {

    @Autowired
    private UserRepository userRepository;

    Instant now = Instant.now();
    Instant expiresAt = now.plus(120, ChronoUnit.MINUTES);
    private Algorithm algorithm = Algorithm.HMAC256("secret");

    public TokenResponse createToken(User user) {
        var jwt = JWT.create()
                .withSubject(user.getId().toString())
                .withClaim("email", user.getEmail())
                .withExpiresAt(Date.from(expiresAt))
                .sign(algorithm);

        long expiresInMinutes = Duration.between(now, expiresAt).toMinutes();

        return new TokenResponse(jwt, user.getEmail(), expiresInMinutes);
    }

    public User getUserFromToken(String jwt) {
        var jwtVerified = JWT.require(algorithm).build().verify(jwt);

        return User.builder()
                .id(Long.valueOf(jwtVerified.getSubject()))
                .email(jwtVerified.getClaim("email").asString())
                .build();
    }
}
