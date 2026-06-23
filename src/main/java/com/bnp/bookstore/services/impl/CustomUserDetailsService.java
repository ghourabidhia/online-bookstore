package com.bnp.bookstore.services.impl;


import com.bnp.bookstore.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;


/**
 * Loads a user from the database by email address.
 * Spring Security calls this during login and on every request with a JWT token.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    /**
     * Find a user by their email address.
     * Throws an error if no user with that email exists.
     */
    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        return userRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("Authentication failed — no user found with email: {}", email);
            return new UsernameNotFoundException("User not found");
        });
    }

}
