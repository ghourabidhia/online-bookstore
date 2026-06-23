package com.bnp.bookstore.services.impl;


import com.bnp.bookstore.dto.request.AddCartItemRequest;
import com.bnp.bookstore.dto.request.UpdateCartItemRequest;
import com.bnp.bookstore.dto.response.CartItemResponse;
import com.bnp.bookstore.dto.response.CartResponse;


import com.bnp.bookstore.entities.Book;
import com.bnp.bookstore.entities.CartItem;
import com.bnp.bookstore.entities.User;
import com.bnp.bookstore.exception.BusinessException;
import com.bnp.bookstore.exception.ResourceNotFoundException;


import com.bnp.bookstore.repositories.BookRepository;
import com.bnp.bookstore.repositories.CartItemRepository;
import com.bnp.bookstore.repositories.UserRepository;
import com.bnp.bookstore.services.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.List;


/**
 * Handles shopping cart actions: view, add, update, and remove books.
 * Always checks that the user owns the cart item before changing it.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    /**
     * Get all items in the user's cart and the total price.
     */
    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        log.debug("Fetching cart for user id={}", userId);

        List<CartItem> cartItems = cartRepository.findByUserId(userId);
        log.debug("Cart for user id={} contains {} item(s)", userId, cartItems.size());

        BigDecimal totalPrice = calculateTotal(cartItems);
        List<CartItemResponse> items = cartItems.stream().map(this::toResponse).toList();

        return new CartResponse(items, totalPrice);
    }


    /**
     * Add a book to the cart. If the book is already there, increase the quantity.
     * Fails if the requested quantity is more than what's in stock.
     */
    @Override
    public void addItem(Long userId, AddCartItemRequest request) {
        log.info("User id={} adding book id={} (qty={}) to cart", userId, request.bookId(), request.quantity());

        User user = findUser(userId);
        Book book = findBook(request.bookId());

        // Check stock before looking up the existing cart item
        validateStock(book, request.quantity());

        CartItem existingItem = cartRepository.findByUserIdAndBookId(userId, book.getId()).orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.quantity();
            log.debug("Book id={} already in cart — merging qty {} + {} = {}", book.getId(),
                    existingItem.getQuantity(), request.quantity(), newQuantity);

            // Check that the combined quantity still fits in stock
            validateStock(book, newQuantity);

            existingItem.setQuantity(newQuantity);
            cartRepository.save(existingItem);
            log.info("Updated cart item id={} to qty={} for user id={}", existingItem.getId(), newQuantity, userId);
            return;
        }

        CartItem item = CartItem.builder()
                .user(user)
                .book(book)
                .quantity(request.quantity())
                .build();

        cartRepository.save(item);
        log.info("Added new cart item — book '{}' (id={}) qty={} for user id={}", book.getTitle(), book.getId(), request.quantity(), userId);
    }

    /**
     * Change how many copies of a book are in the cart.
     * Fails if the new quantity is more than what's in stock.
     */
    @Override
    public void updateQuantity(Long userId, Long itemId, UpdateCartItemRequest request) {
        log.info("User id={} updating cart item id={} to qty={}", userId, itemId, request.quantity());

        CartItem item = findCartItem(userId, itemId);
        validateStock(item.getBook(), request.quantity());

        item.setQuantity(request.quantity());
        cartRepository.save(item);

        log.info("Cart item id={} updated to qty={} for user id={}", itemId, request.quantity(), userId);
    }

    /**
     * Remove a book from the cart.
     */
    @Override
    public void removeItem(Long userId, Long itemId) {
        log.info("User id={} removing cart item id={}", userId, itemId);

        CartItem item = findCartItem(userId, itemId);
        cartRepository.delete(item);

        log.info("Cart item id={} removed for user id={}", itemId, userId);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    /** Add up the price × quantity for every item in the cart. */
    private BigDecimal calculateTotal(List<CartItem> items) {
        return items.stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Turn a CartItem into the response object sent to the frontend. */
    private CartItemResponse toResponse(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getBook().getTitle(),
                item.getQuantity(),
                item.getBook().getPrice()
        );
    }

    /** Find a user by id, or throw an error if not found. */
    private User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User not found: id={}", userId);
            return new ResourceNotFoundException("User not found ");
        });
    }

    /** Find a book by id, or throw an error if not found. */
    private Book findBook(Long bookId) {
        return bookRepository.findById(bookId).orElseThrow(() -> {
            log.warn("Book not found: id={}", bookId);
            return new ResourceNotFoundException("Book not found");
        });
    }

    /**
     * Find a cart item that belongs to this user.
     * Using both the item id and user id prevents one user from touching another user's cart.
     */
    private CartItem findCartItem(Long userId, Long itemId) {
        return cartRepository.findByIdAndUserId(itemId, userId).orElseThrow(() -> {
            log.warn("Cart item not found: itemId={}, userId={}", itemId, userId);
            return new ResourceNotFoundException("Cart item not found");
        });
    }

    /**
     * Make sure the quantity is positive and doesn't exceed available stock.
     * Throws an error if the check fails.
     */
    private void validateStock(Book book, Integer quantity) {
        if (quantity <= 0) {
            throw new BusinessException("Quantity must be greater than zero");
        }
        if (quantity > book.getStock()) {
            log.warn("Stock validation failed — book '{}' (id={}) has {} in stock, requested {}",
                    book.getTitle(), book.getId(), book.getStock(), quantity);
            throw new BusinessException("Requested quantity exceeds available stock");
        }
    }
}
