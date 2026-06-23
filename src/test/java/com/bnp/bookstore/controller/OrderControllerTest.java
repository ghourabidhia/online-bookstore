package com.bnp.bookstore.controller;

import com.bnp.bookstore.dto.response.OrderItemResponse;
import com.bnp.bookstore.dto.response.OrderResponse;
import com.bnp.bookstore.entities.Role;
import com.bnp.bookstore.entities.User;
import com.bnp.bookstore.exception.BusinessException;
import com.bnp.bookstore.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class OrderControllerTest {

    @Autowired
    WebApplicationContext context;
    @MockitoBean
    OrderService orderService;

    private MockMvc mockMvc;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        User authenticatedUser = User.builder()
                .id(1L).email("user@test.com").role(Role.USER)
                .firstname("Test").lastname("User").password("encoded").build();

        auth = new UsernamePasswordAuthenticationToken(
                authenticatedUser, null, authenticatedUser.getAuthorities()
        );
    }

    // ── POST /api/orders ──────────────────────────────────────

    @Test
    void createOrder_authenticated_returns201WithOrderResponse() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        OrderItemResponse item = new OrderItemResponse("Effective Java", 2, new BigDecimal("59.99"));
        OrderResponse order = new OrderResponse(1L, new BigDecimal("119.98"), now, List.of(item));

        when(orderService.createOrder(1L)).thenReturn(order);

        mockMvc.perform(post("/api/orders").with(authentication(auth)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.totalPrice").value(119.98))
                .andExpect(jsonPath("$.items[0].bookTitle").value("Effective Java"));
    }

    @Test
    void createOrder_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createOrder_emptyCart_returns400() throws Exception {
        when(orderService.createOrder(1L))
                .thenThrow(new BusinessException("Cart is empty"));

        mockMvc.perform(post("/api/orders").with(authentication(auth)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cart is empty"));
    }

    @Test
    void createOrder_insufficientStock_returns400() throws Exception {
        when(orderService.createOrder(1L))
                .thenThrow(new BusinessException("Insufficient stock for book: Effective Java"));

        mockMvc.perform(post("/api/orders").with(authentication(auth)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient stock for book: Effective Java"));
    }

    // ── GET /api/orders ───────────────────────────────────────

    @Test
    void getOrders_authenticated_returns200WithOrderList() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        OrderResponse o1 = new OrderResponse(1L, new BigDecimal("50.00"), now.minusDays(1), List.of());
        OrderResponse o2 = new OrderResponse(2L, new BigDecimal("30.00"), now, List.of());

        when(orderService.getOrdersByUser(1L)).thenReturn(List.of(o2, o1));

        mockMvc.perform(get("/api/orders").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].id").value(1));
    }

    @Test
    void getOrders_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getOrders_noOrders_returns200WithEmptyList() throws Exception {
        when(orderService.getOrdersByUser(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/api/orders").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
