package com.bnp.bookstore.exception;


/** Thrown when a business rule is broken, for example: not enough stock, or empty cart. */
public class BusinessException
        extends RuntimeException {


    public BusinessException(
            String message
    ) {

        super(message);

    }

}