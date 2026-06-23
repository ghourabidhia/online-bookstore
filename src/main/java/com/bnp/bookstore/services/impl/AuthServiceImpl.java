package com.bnp.bookstore.services.impl;


import com.bnp.bookstore.dto.request.LoginRequest;
import com.bnp.bookstore.dto.request.RegisterRequest;
import com.bnp.bookstore.dto.response.AuthResponse;


import com.bnp.bookstore.entities.Role;
import com.bnp.bookstore.entities.User;
import com.bnp.bookstore.exception.BusinessException;

import com.bnp.bookstore.mapper.UserMapper;


import com.bnp.bookstore.repositories.UserRepository;
import com.bnp.bookstore.security.JwtService;
import com.bnp.bookstore.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


/**
 * Handles user registration and login.
 * Saves the new user, hashes the password, and returns a JWT token.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {


    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    /**
     * Create a new user account and return a JWT token.
     * Fails if the email is already taken.
     */
    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.email());

        userRepository.findByEmail(request.email()).ifPresent(existing -> {
            log.warn("Registration rejected — email already in use: {}", request.email());
            throw new BusinessException("Email already exists");
        });

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        userRepository.save(user);

        log.info("User registered successfully: {}", request.email());

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }


    /**
     * Check the user's email and password, then return a JWT token.
     * Throws an error if the credentials are wrong.
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        // Throws BadCredentialsException if the email or password is wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> {
            log.warn("Login failed — user not found after authentication: {}", request.email());
            return new BusinessException("User not found");
        });

        log.info("Login successful for email: {}", request.email());
        return new AuthResponse(jwtService.generateToken(user));
    }

}
