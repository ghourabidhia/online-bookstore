package com.bnp.bookstore.services.impl;


import com.bnp.bookstore.dto.response.OrderResponse;
import com.bnp.bookstore.entities.*;
import com.bnp.bookstore.exception.BusinessException;
import com.bnp.bookstore.exception.ResourceNotFoundException;
import com.bnp.bookstore.mapper.OrderMapper;
import com.bnp.bookstore.repositories.CartItemRepository;
import com.bnp.bookstore.repositories.OrderRepository;
import com.bnp.bookstore.repositories.UserRepository;
import com.bnp.bookstore.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/**
 * Handles placing orders.
 * Checks stock, saves the order, reduces stock, and clears the cart — all in one transaction.
 * If anything goes wrong, everything is rolled back so nothing is saved partially.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {


    private final CartItemRepository cartRepository;

    private final UserRepository userRepository;

    private final OrderRepository orderRepository;

    private final OrderMapper mapper;


    /**
     * Turn the user's cart into a saved order.
     * Fails if the cart is empty or any book doesn't have enough stock.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder(Long userId) {
        log.info("Order creation started for user id={}", userId);

        User user = findUser(userId);

        List<CartItem> cartItems = getCartItems(userId);
        log.debug("Cart contains {} item(s) for user id={}", cartItems.size(), userId);

        validateStock(cartItems);
        log.debug("Stock validation passed for user id={}", userId);

        Order order = buildOrder(user, cartItems);

        // Reduce stock before saving — Hibernate saves the changes automatically at the end
        updateStock(cartItems);

        Order savedOrder = orderRepository.save(order);
        log.info("Order id={} persisted for user id={} — total: {}", savedOrder.getId(), userId, savedOrder.getTotalPrice());

        clearCart(cartItems);
        log.debug("Cart cleared for user id={} after order id={}", userId, savedOrder.getId());

        return mapper.toResponse(savedOrder);
    }


    /**
     * Get all orders for a user, newest first.
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(Long userId) {
        log.debug("Fetching order history for user id={}", userId);

        List<OrderResponse> orders = orderRepository
                .findByUserIdOrderByOrderDateDesc(userId)
                .stream()
                .map(mapper::toResponse)
                .toList();

        log.debug("Found {} order(s) for user id={}", orders.size(), userId);
        return orders;
    }

    // ── private helpers ───────────────────────────────────────────────────────

    /** Find a user by id, or throw an error if not found. */
    private User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Order creation aborted — user not found: id={}", userId);
            return new ResourceNotFoundException("User not found");
        });
    }

    /**
     * Load the cart items and make sure there is at least one item.
     * Throws an error if the cart is empty.
     */
    private List<CartItem> getCartItems(Long userId) {
        List<CartItem> cartItems = cartRepository.findByUserId(userId);

        if (cartItems.isEmpty()) {
            log.warn("Order creation aborted — cart is empty for user id={}", userId);
            throw new BusinessException("Cart is empty");
        }

        return cartItems;
    }

    /**
     * Check that every book in the cart has enough copies in stock.
     * Throws an error for the first book that doesn't.
     */
    private void validateStock(List<CartItem> cartItems) {
        cartItems.forEach(item -> {
            if (item.getBook().getStock() < item.getQuantity()) {
                log.warn("Insufficient stock — book '{}' (id={}) has {} in stock, cart requires {}",
                        item.getBook().getTitle(), item.getBook().getId(),
                        item.getBook().getStock(), item.getQuantity());
                throw new BusinessException("Insufficient stock for book: " + item.getBook().getTitle());
            }
        });
    }

    /** Build the order object with all its items. Does not save it yet. */
    private Order buildOrder(User user, List<CartItem> cartItems) {
        Order order = Order.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .totalPrice(calculateTotal(cartItems))
                .build();

        cartItems.stream()
                .map(this::buildOrderItem)
                .forEach(order::addItem);

        return order;
    }

    /** Copy a cart item into an order item, locking in the current price. */
    private OrderItem buildOrderItem(CartItem cartItem) {
        return OrderItem.builder()
                .book(cartItem.getBook())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getBook().getPrice())
                .build();
    }

    /**
     * Reduce the stock for each book that was ordered.
     * No manual save needed — Hibernate saves the changes at the end of the transaction.
     */
    private void updateStock(List<CartItem> cartItems) {
        cartItems.forEach(item -> {
            Book book = item.getBook();
            int newStock = book.getStock() - item.getQuantity();
            log.debug("Decrementing stock for book '{}' (id={}): {} → {}", book.getTitle(), book.getId(), book.getStock(), newStock);
            book.setStock(newStock);
        });
    }

    /** Delete all cart items after the order is saved. */
    private void clearCart(List<CartItem> cartItems) {
        cartRepository.deleteAll(cartItems);
    }

    /** Add up price × quantity for every item to get the order total. */
    private BigDecimal calculateTotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


}
