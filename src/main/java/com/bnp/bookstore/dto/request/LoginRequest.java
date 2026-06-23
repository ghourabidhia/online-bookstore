package com.bnp.bookstore.dto.request;




import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


/** Data sent by the user to log in. */
public record LoginRequest(


        @Email
        @NotBlank
        String email,


        @NotBlank
        String password


) {
}