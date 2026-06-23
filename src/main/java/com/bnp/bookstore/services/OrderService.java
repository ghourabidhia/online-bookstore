package com.bnp.bookstore.services;


import com.bnp.bookstore.dto.response.OrderResponse;

import java.util.List;


public interface OrderService {

    /** Place an order using everything in the user's cart. */
    OrderResponse createOrder(Long userId);

    /** Get all orders for a user, newest first. */
    List<OrderResponse> getOrdersByUser(Long userId);

}