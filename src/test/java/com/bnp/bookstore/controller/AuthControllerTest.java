package com.bnp.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bnp.bookstore.controllers.AuthController;
import com.bnp.bookstore.dto.request.LoginRequest;
import com.bnp.bookstore.dto.request.RegisterRequest;
import com.bnp.bookstore.dto.response.AuthResponse;
import com.bnp.bookstore.exception.BusinessException;
import com.bnp.bookstore.security.JwtService;
import com.bnp.bookstore.security.SecurityConfig;
import com.bnp.bookstore.services.AuthService;
import com.bnp.bookstore.services.impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean AuthService authService;
    @MockitoBean JwtService jwtService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    // ── register ──────────────────────────────────────────────

    @Test
    void register_validRequest_returns201WithToken() throws Exception {
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "password123");
        when(authService.register(any())).thenReturn(new AuthResponse("jwt-token"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void register_blankFirstname_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("", "Doe", "john@example.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_blankLastname_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("John", "", "john@example.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("John", "Doe", "not-an-email", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("John", "Doe", "john@example.com", "short");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_duplicateEmail_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest("John", "Doe", "taken@example.com", "password123");
        when(authService.register(any())).thenThrow(new BusinessException("Email already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    // ── login ─────────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        when(authService.login(any())).thenReturn(new AuthResponse("jwt-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void login_invalidEmail_returns400() throws Exception {
        LoginRequest request = new LoginRequest("not-an-email", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_blankPassword_returns400() throws Exception {
        LoginRequest request = new LoginRequest("john@example.com", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_badCredentials_returns400() throws Exception {
        LoginRequest request = new LoginRequest("john@example.com", "wrongpass");
        when(authService.login(any())).thenThrow(new BusinessException("User not found"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
