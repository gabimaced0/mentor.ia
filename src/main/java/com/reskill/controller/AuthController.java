package com.reskill.controller;

import com.reskill.dto.auth.Credentials;
import com.reskill.dto.auth.TokenResponse;
import com.reskill.model.User;
import com.reskill.service.security.AuthService;
import com.reskill.service.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public TokenResponse login(@RequestBody Credentials credentials){
        User user = (User) authService.loadUserByUsername(credentials.login());
        if (!passwordEncoder.matches(credentials.password(), user.getPassword())){
            throw new BadCredentialsException("Senha incorreta");
        }
        return tokenService.createToken(user);
    }
}
