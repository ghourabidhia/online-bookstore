package com.bnp.bookstore.security;


import com.bnp.bookstore.services.impl.CustomUserDetailsService;
import jakarta.servlet.FilterChain;

import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContextHolder;


import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Component;


import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;


/**
 * Runs on every HTTP request and checks for a JWT token in the Authorization header.
 * If the token is valid, the user is marked as logged in for that request.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtService jwtService;

    private final CustomUserDetailsService userDetailsService;


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        log.debug("JWT filter processing: {} {}", request.getMethod(), request.getRequestURI());

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token found — pass the request through unchanged
            log.debug("No Bearer token on {} {} — skipping JWT auth", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // Remove the "Bearer " prefix to get the raw token string
        String token = authHeader.substring(7);

        String email = jwtService.extractUsername(token);
        log.debug("JWT token found — extracted email: {}", email);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails user = userDetailsService.loadUserByUsername(email);

            if (jwtService.isValid(token, user)) {
                // Mark this user as logged in for the current request
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authentication set in SecurityContext for user: {}", email);
            } else {
                log.warn("JWT token rejected for email: {} on {} {}", email, request.getMethod(), request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }

}
