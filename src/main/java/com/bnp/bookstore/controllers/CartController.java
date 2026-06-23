package com.bnp.bookstore.controllers;

import com.bnp.bookstore.dto.request.AddCartItemRequest;
import com.bnp.bookstore.dto.request.UpdateCartItemRequest;
import com.bnp.bookstore.dto.response.CartResponse;
import com.bnp.bookstore.entities.User;
import com.bnp.bookstore.exception.UnauthorizedException;
import com.bnp.bookstore.services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


/** Handles HTTP requests to view and change the shopping cart. Requires login. */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management")
public class CartController {


    private final CartService cartService;


    @GetMapping
    @Operation(summary = "Get current user cart")
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                cartService.getCart(getAuthenticatedUserId(user))
        );
    }


    @PostMapping("/items")
    @Operation(summary = "Add book to cart")
    public ResponseEntity<Void> addItem(@AuthenticationPrincipal User user, @Valid @RequestBody AddCartItemRequest request) {

        cartService.addItem(
                getAuthenticatedUserId(user),
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PutMapping("/items/{id}")
    @Operation(summary = "Update cart quantity")
    public ResponseEntity<Void> update(@AuthenticationPrincipal User user, @PathVariable Long id, @Valid @RequestBody UpdateCartItemRequest request) {

        cartService.updateQuantity(
                getAuthenticatedUserId(user),
                id,
                request
        );

        return ResponseEntity.noContent().build();

    }


    @DeleteMapping("/items/{id}")
    @Operation(summary = "Remove cart item")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable Long id) {

        cartService.removeItem(
                getAuthenticatedUserId(user),
                id
        );

        return ResponseEntity.noContent().build();

    }

    private Long getAuthenticatedUserId(User user) {
        if (user == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        return user.getId();
    }

}