package com.bnp.bookstore.dto.response;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * Order returned to client.
 */
public record OrderResponse(


        Long id,


        BigDecimal totalPrice,


        LocalDateTime orderDate,


        List<OrderItemResponse> items


) {
}