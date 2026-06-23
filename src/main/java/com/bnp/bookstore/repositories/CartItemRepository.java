package com.bnp.bookstore.repositories;

import com.bnp.bookstore.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserId(Long userId);

    Optional<CartItem> findByIdAndUserId(Long itemId, Long userId);

    Optional<CartItem> findByUserIdAndBookId(Long userId, Long bookId);
}
