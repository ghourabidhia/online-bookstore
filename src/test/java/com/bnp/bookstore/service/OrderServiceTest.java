package com.bnp.bookstore.service;

import com.bnp.bookstore.dto.response.OrderResponse;
import com.bnp.bookstore.entities.*;
import com.bnp.bookstore.exception.BusinessException;
import com.bnp.bookstore.exception.ResourceNotFoundException;
import com.bnp.bookstore.mapper.OrderMapper;
import com.bnp.bookstore.repositories.CartItemRepository;
import com.bnp.bookstore.repositories.OrderRepository;
import com.bnp.bookstore.repositories.UserRepository;
import com.bnp.bookstore.services.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    CartItemRepository cartRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    OrderRepository orderRepository;
    @Mock
    OrderMapper mapper;
    @InjectMocks
    OrderServiceImpl orderService;

    private User user;
    private Book book;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).email("user@test.com").role(Role.USER)
                .firstname("Test").lastname("User").build();

        book = Book.builder()
                .id(1L).title("Effective Java").author("Bloch")
                .price(new BigDecimal("59.99")).stock(10).build();

        cartItem = CartItem.builder()
                .id(1L).user(user).book(book).quantity(2).build();
    }

    // ── createOrder ───────────────────────────────────────────

    @Test
    void createOrder_validCart_returnsOrderResponse() {
        Order saved = Order.builder()
                .id(1L).user(user)
                .totalPrice(new BigDecimal("119.98"))
                .orderDate(LocalDateTime.now())
                .build();
        OrderResponse expected = new OrderResponse(
                1L, new BigDecimal("119.98"), saved.getOrderDate(), List.of()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(expected);

        OrderResponse result = orderService.createOrder(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.totalPrice()).isEqualByComparingTo("119.98");
    }

    @Test
    void createOrder_validCart_clearsCartAfterSave() {
        Order saved = Order.builder()
                .id(1L).user(user)
                .totalPrice(new BigDecimal("119.98"))
                .orderDate(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(
                new OrderResponse(1L, new BigDecimal("119.98"), saved.getOrderDate(), List.of())
        );

        orderService.createOrder(1L);

        verify(cartRepository).deleteAll(List.of(cartItem));
    }

    @Test
    void createOrder_validCart_deductsStock() {
        int initialStock = book.getStock(); // 10, cart qty is 2
        Order saved = Order.builder()
                .id(1L).user(user)
                .totalPrice(new BigDecimal("119.98"))
                .orderDate(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(List.of(cartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(
                new OrderResponse(1L, new BigDecimal("119.98"), saved.getOrderDate(), List.of())
        );

        orderService.createOrder(1L);

        assertThat(book.getStock()).isEqualTo(initialStock - 2);
    }

    @Test
    void createOrder_emptyCart_throwsBusinessException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.createOrder(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cart is empty");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_insufficientStock_throwsBusinessException() {
        book.setStock(1); // cart has qty 2, only 1 in stock
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(List.of(cartItem));

        assertThatThrownBy(() -> orderService.createOrder(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient stock for book: Effective Java");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(cartRepository, never()).findByUserId(any());
    }

    // ── getOrdersByUser ───────────────────────────────────────

    @Test
    void getOrdersByUser_returnsAllOrdersMappedToResponse() {
        LocalDateTime now = LocalDateTime.now();
        Order o1 = Order.builder().id(1L).user(user)
                .totalPrice(new BigDecimal("50.00")).orderDate(now.minusDays(1)).build();
        Order o2 = Order.builder().id(2L).user(user)
                .totalPrice(new BigDecimal("30.00")).orderDate(now).build();

        OrderResponse r1 = new OrderResponse(1L, new BigDecimal("50.00"), o1.getOrderDate(), List.of());
        OrderResponse r2 = new OrderResponse(2L, new BigDecimal("30.00"), o2.getOrderDate(), List.of());

        when(orderRepository.findByUserIdOrderByOrderDateDesc(1L)).thenReturn(List.of(o2, o1));
        when(mapper.toResponse(o2)).thenReturn(r2);
        when(mapper.toResponse(o1)).thenReturn(r1);

        List<OrderResponse> result = orderService.getOrdersByUser(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(2L); // newest first
        assertThat(result.get(1).id()).isEqualTo(1L);
    }

    @Test
    void getOrdersByUser_noOrders_returnsEmptyList() {
        when(orderRepository.findByUserIdOrderByOrderDateDesc(1L)).thenReturn(List.of());

        List<OrderResponse> result = orderService.getOrdersByUser(1L);

        assertThat(result).isEmpty();
    }
}
