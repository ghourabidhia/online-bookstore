package com.bnp.bookstore.exception;


import java.time.LocalDateTime;


/** The error data sent back to the client when something goes wrong. */
public record ErrorResponse(


        LocalDateTime timestamp,


        int status,


        String message,


        String path


) {


}
