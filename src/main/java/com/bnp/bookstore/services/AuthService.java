package com.bnp.bookstore.services;


import com.bnp.bookstore.dto.request.LoginRequest;
import com.bnp.bookstore.dto.request.RegisterRequest;
import com.bnp.bookstore.dto.response.AuthResponse;


/** Defines the register and login actions. */
public interface AuthService {


    AuthResponse register(
            RegisterRequest request
    );


    AuthResponse login(
            LoginRequest request
    );


}
