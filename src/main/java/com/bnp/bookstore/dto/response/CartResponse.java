package com.bnp.bookstore.dto.response;


import java.math.BigDecimal;
import java.util.List;


/**
 * Complete shopping cart response.
 */
public record CartResponse(List<CartItemResponse> items, BigDecimal totalPrice) {
}
