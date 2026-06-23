package com.bnp.bookstore.exception;


/** Thrown when something (like a book or user) is not found in the database. */
public class ResourceNotFoundException
        extends RuntimeException {


    public ResourceNotFoundException(
            String message
    ) {

        super(message);

    }

}