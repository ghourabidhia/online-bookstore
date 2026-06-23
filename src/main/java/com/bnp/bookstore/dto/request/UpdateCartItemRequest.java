package com.bnp.bookstore.dto.request;


import jakarta.validation.constraints.Min;


/** Data sent to change how many copies of a book are in the cart. */
public record UpdateCartItemRequest(


        @Min(
                value = 1,
                message = "Quantity must be positive"
        )
        Integer quantity


) {
}