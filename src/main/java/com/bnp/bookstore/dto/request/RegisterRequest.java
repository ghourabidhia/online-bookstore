package com.bnp.bookstore.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Data sent by the user to create a new account. */
public record RegisterRequest(


        @NotBlank(
                message = "Firstname is mandatory"
        )
        String firstname,


        @NotBlank(
                message = "Lastname is mandatory"
        )
        String lastname,


        @NotBlank(
                message = "Email is mandatory"
        )
        @Email(
                message = "Email format is invalid"
        )
        String email,


        @NotBlank(
                message = "Password is mandatory"
        )
        @Size(
                min = 8,
                message = "Password must contain minimum 8 characters"
        )
        String password


) {
}
