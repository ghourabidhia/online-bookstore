package com.bnp.bookstore.controller;

import com.bnp.bookstore.controllers.CartController;
import com.bnp.bookstore.dto.request.AddCartItemRequest;
import com.bnp.bookstore.dto.request.UpdateCartItemRequest;
import com.bnp.bookstore.dto.response.CartItemResponse;
import com.bnp.bookstore.dto.response.CartResponse;
import com.bnp.bookstore.entities.Role;
import com.bnp.bookstore.entities.User;
import com.bnp.bookstore.exception.BusinessException;
import com.bnp.bookstore.exception.ResourceNotFoundException;
import com.bnp.bookstore.security.JwtService;
import com.bnp.bookstore.security.SecurityConfig;
import com.bnp.bookstore.services.CartService;
import com.bnp.bookstore.services.impl.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@Import(SecurityConfig.class)
class CartControllerTest {

    @Autowired
    MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    CartService cartService;
    @MockitoBean
    JwtService jwtService;
    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setup() {
        User user = User.builder()
                .id(1L).email("user@test.com").role(Role.USER)
                .firstname("Test").lastname("User").password("encoded").build();

        auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    // ── GET /api/cart ─────────────────────────────────────────

    @Test
    void getCart_authenticated_returns200WithCartResponse() throws Exception {
        CartItemResponse item = new CartItemResponse(1L, "Spring in Action", 2, new BigDecimal("49.99"));
        CartResponse cartResponse = new CartResponse(List.of(item), new BigDecimal("99.98"));
        when(cartService.getCart(1L)).thenReturn(cartResponse);

        mockMvc.perform(get("/api/cart").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].bookTitle").value("Spring in Action"))
                .andExpect(jsonPath("$.totalPrice").value(99.98));
    }

    @Test
    void getCart_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCart_emptyCart_returns200WithEmptyItems() throws Exception {
        when(cartService.getCart(1L)).thenReturn(new CartResponse(List.of(), BigDecimal.ZERO));

        mockMvc.perform(get("/api/cart").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    // ── POST /api/cart/items ──────────────────────────────────

    @Test
    void addItem_authenticated_returns201() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest(1L, 2);

        mockMvc.perform(post("/api/cart/items")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void addItem_nullBookId_returns400() throws Exception {
        String invalidJson = """
                {"bookId": null, "quantity": 2}
                """;

        mockMvc.perform(post("/api/cart/items")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_quantityLessThanOne_returns400() throws Exception {
        String invalidJson = """
                {"bookId": 1, "quantity": 0}
                """;

        mockMvc.perform(post("/api/cart/items")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItem_insufficientStock_returns400() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest(1L, 100);
        doThrow(new BusinessException("Requested quantity exceeds available stock"))
                .when(cartService).addItem(anyLong(), any());

        mockMvc.perform(post("/api/cart/items")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Requested quantity exceeds available stock"));
    }

    @Test
    void addItem_bookNotFound_returns404() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest(999L, 1);
        doThrow(new ResourceNotFoundException("Book not found"))
                .when(cartService).addItem(anyLong(), any());

        mockMvc.perform(post("/api/cart/items")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── PUT /api/cart/items/{id} ──────────────────────────────

    @Test
    void update_authenticated_returns204() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest(3);

        mockMvc.perform(put("/api/cart/items/1")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void update_quantityLessThanOne_returns400() throws Exception {
        String invalidJson = """
                {"quantity": 0}
                """;

        mockMvc.perform(put("/api/cart/items/1")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_itemNotFound_returns404() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest(2);
        doThrow(new ResourceNotFoundException("Cart item not found"))
                .when(cartService).updateQuantity(anyLong(), anyLong(), any());

        mockMvc.perform(put("/api/cart/items/99")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/cart/items/{id} ───────────────────────────

    @Test
    void delete_authenticated_returns204() throws Exception {
        mockMvc.perform(delete("/api/cart/items/1").with(authentication(auth)))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_itemNotFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Cart item not found"))
                .when(cartService).removeItem(anyLong(), anyLong());

        mockMvc.perform(delete("/api/cart/items/99").with(authentication(auth)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_unauthenticated_returns403() throws Exception {
        mockMvc.perform(delete("/api/cart/items/1"))
                .andExpect(status().isForbidden());
    }
}
