package com.bnp.bookstore.repositories;

import com.bnp.bookstore.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface OrderRepository extends JpaRepository<Order, Long> {

    /** Get all orders for a user, sorted by date with the newest first. */
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

}
