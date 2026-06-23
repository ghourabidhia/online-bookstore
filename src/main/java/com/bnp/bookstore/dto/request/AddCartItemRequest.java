package com.bnp.bookstore.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


/** Data sent to add a book to the cart. */
public record AddCartItemRequest(

        @NotNull(
                message = "Book id is mandatory"
        )
        Long bookId,


        @Min(
                value = 1,
                message = "Quantity must be greater than zero"
        )
        Integer quantity


) {
}