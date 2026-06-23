package com.bnp.bookstore.repository;

import com.bnp.bookstore.entities.Order;
import com.bnp.bookstore.entities.Role;
import com.bnp.bookstore.entities.User;
import com.bnp.bookstore.repositories.OrderRepository;
import com.bnp.bookstore.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired OrderRepository orderRepository;
    @Autowired UserRepository userRepository;

    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .firstname("Alice").lastname("Smith")
                .email("alice@test.com").password("hashed").role(Role.USER).build());

        otherUser = userRepository.save(User.builder()
                .firstname("Bob").lastname("Jones")
                .email("bob@test.com").password("hashed").role(Role.USER).build());
    }

    private Order order(User owner, BigDecimal price, LocalDateTime date) {
        return Order.builder()
                .user(owner)
                .totalPrice(price)
                .orderDate(date)
                .build();
    }

    // ── findByUserIdOrderByOrderDateDesc ──────────────────────

    @Test
    void findByUserIdOrderByOrderDateDesc_returnsOnlyUserOrders() {
        LocalDateTime now = LocalDateTime.now();
        orderRepository.save(order(user, new BigDecimal("50.00"), now));
        orderRepository.save(order(otherUser, new BigDecimal("30.00"), now));

        List<Order> result = orderRepository.findByUserIdOrderByOrderDateDesc(user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void findByUserIdOrderByOrderDateDesc_orderedNewestFirst() {
        LocalDateTime base = LocalDateTime.now();
        orderRepository.save(order(user, new BigDecimal("10.00"), base.minusDays(2)));
        orderRepository.save(order(user, new BigDecimal("20.00"), base));
        orderRepository.save(order(user, new BigDecimal("30.00"), base.minusDays(1)));

        List<Order> result = orderRepository.findByUserIdOrderByOrderDateDesc(user.getId());

        assertThat(result).hasSize(3);
        // newest first: base, base-1day, base-2days
        assertThat(result.get(0).getTotalPrice()).isEqualByComparingTo("20.00");
        assertThat(result.get(1).getTotalPrice()).isEqualByComparingTo("30.00");
        assertThat(result.get(2).getTotalPrice()).isEqualByComparingTo("10.00");
    }

    @Test
    void findByUserIdOrderByOrderDateDesc_noOrders_returnsEmptyList() {
        List<Order> result = orderRepository.findByUserIdOrderByOrderDateDesc(user.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserIdOrderByOrderDateDesc_singleOrder_returnsSingleElement() {
        orderRepository.save(order(user, new BigDecimal("99.00"), LocalDateTime.now()));

        List<Order> result = orderRepository.findByUserIdOrderByOrderDateDesc(user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTotalPrice()).isEqualByComparingTo("99.00");
    }
}
