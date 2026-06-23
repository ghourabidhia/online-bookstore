package com.bnp.bookstore.dto.response;


import java.math.BigDecimal;

/** Book data sent to the frontend. Includes stock so the UI can disable the buy button when empty. */
public record BookResponse(

        Long id,

        String title,

        String author,

        BigDecimal price,

        /** How many copies are available to buy. */
        Integer stock

) {
}
