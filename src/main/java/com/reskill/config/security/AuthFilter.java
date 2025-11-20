package com.reskill.config.security;

import com.reskill.service.security.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;


    private final List<String> PUBLIC_PATHS = Arrays.asList(
            "/user",
            "/login",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-resources",
            "/webjars",
            "/configuration"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();


        if (isPublicPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        var header = request.getHeader("Authorization");
        if(header == null){
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write(""" 
                {"message": "Token não fornecido"} 
            """);
            return;
        }

        if(!header.startsWith("Bearer ")){
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write(""" 
                {"message": "Header deve iniciar com Bearer"} 
            """);
            return;
        }

        var jwt = header.replace("Bearer ", "");

        try {
            var user = tokenService.getUserFromToken(jwt);
            var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write(""" 
                {"message": "Token inválido: " + e.getMessage()} 
            """);
        }
    }

    private boolean isPublicPath(String requestURI) {
        return PUBLIC_PATHS.stream().anyMatch(requestURI::startsWith);
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return PUBLIC_PATHS.stream().anyMatch(requestURI::startsWith);
    }
}