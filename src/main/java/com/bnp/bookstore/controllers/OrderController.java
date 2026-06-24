package com.bnp.bookstore.controllers;


import com.bnp.bookstore.dto.response.OrderResponse;
import com.bnp.bookstore.entities.User;
import com.bnp.bookstore.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/** Handles HTTP requests to place and view orders. Requires login. */
@SecurityRequirement(name = "bearer-key")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Customer order management"
)
public class OrderController {


    private final OrderService orderService;


    /** Place a new order using everything in the current cart. */
    @PostMapping
    @Operation(
            summary = "Create a new order"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order placed successfully"),
            @ApiResponse(responseCode = "400", description = "Cart is empty or a book no longer has enough stock"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    public ResponseEntity<OrderResponse> createOrder(@AuthenticationPrincipal User user) {

        OrderResponse response = orderService.createOrder(user.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }


    /** Get all orders for the logged-in user, newest first. */
    @GetMapping
    @Operation(summary = "Get all orders for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of orders returned successfully"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(@AuthenticationPrincipal User user) {

        return ResponseEntity.ok(orderService.getOrdersByUser(user.getId()));

    }


}
