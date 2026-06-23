package com.bnp.bookstore.dto.response;


import java.math.BigDecimal;

/**
 * Order item information.
 */
public record OrderItemResponse(


        String bookTitle,


        Integer quantity,


        BigDecimal price


) {
}
