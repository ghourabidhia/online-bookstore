package com.bnp.bookstore.dto.response;


import java.math.BigDecimal;

/**
 * Represents an item inside shopping cart.
 */
public record CartItemResponse(


        Long id,


        String bookTitle,


        Integer quantity,


        BigDecimal price


) {
}
