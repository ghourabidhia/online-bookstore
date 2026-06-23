package com.bnp.bookstore.controllers;


import com.bnp.bookstore.dto.request.LoginRequest;
import com.bnp.bookstore.dto.request.RegisterRequest;
import com.bnp.bookstore.dto.response.AuthResponse;
import com.bnp.bookstore.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/** Receives HTTP requests for login and registration. Passes them to the auth service. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "User registration and login"
)
public class AuthController {


    private final AuthService authService;


    /** Create a new account and return a JWT token. */
    @PostMapping("/register")
    @Operation(
            summary = "Register a new customer"
    )
    public ResponseEntity<AuthResponse> register(


            @Valid
            @RequestBody
            RegisterRequest request


    ) {


        return ResponseEntity

                .status(HttpStatus.CREATED)

                .body(
                        authService.register(request)
                );


    }


    /** Log in with email and password and get a JWT token. */
    @PostMapping("/login")
    @Operation(
            summary = "Login and receive JWT token"
    )
    public ResponseEntity<AuthResponse> login(


            @Valid
            @RequestBody
            LoginRequest request


    ) {


        return ResponseEntity.ok(

                authService.login(request)

        );


    }


}