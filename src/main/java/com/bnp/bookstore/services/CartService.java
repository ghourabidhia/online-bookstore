package com.bnp.bookstore.services;


import com.bnp.bookstore.dto.request.AddCartItemRequest;
import com.bnp.bookstore.dto.request.UpdateCartItemRequest;
import com.bnp.bookstore.dto.response.CartResponse;


public interface CartService {


    CartResponse getCart(
            Long userId
    );


    void addItem(
            Long userId,
            AddCartItemRequest request
    );


    void updateQuantity(
            Long userId,
            Long itemId,
            UpdateCartItemRequest request
    );


    void removeItem(
            Long userId,
            Long itemId
    );


}
