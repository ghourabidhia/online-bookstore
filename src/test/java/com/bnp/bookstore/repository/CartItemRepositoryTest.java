package com.bnp.bookstore.repository;

import com.bnp.bookstore.entities.Book;
import com.bnp.bookstore.entities.CartItem;
import com.bnp.bookstore.entities.Role;
import com.bnp.bookstore.entities.User;
import com.bnp.bookstore.repositories.BookRepository;
import com.bnp.bookstore.repositories.CartItemRepository;
import com.bnp.bookstore.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CartItemRepositoryTest {

    @Autowired CartItemRepository cartItemRepository;
    @Autowired UserRepository userRepository;
    @Autowired BookRepository bookRepository;

    private User user;
    private User otherUser;
    private Book book;
    private Book otherBook;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .firstname("Alice").lastname("Smith")
                .email("alice@test.com").password("hashed").role(Role.USER).build());

        otherUser = userRepository.save(User.builder()
                .firstname("Bob").lastname("Jones")
                .email("bob@test.com").password("hashed").role(Role.USER).build());

        book = bookRepository.save(Book.builder()
                .title("Spring in Action").author("Walls")
                .price(new BigDecimal("49.99")).stock(10).build());

        otherBook = bookRepository.save(Book.builder()
                .title("Effective Java").author("Bloch")
                .price(new BigDecimal("59.99")).stock(5).build());
    }

    // ── findByUserId ──────────────────────────────────────────

    @Test
    void findByUserId_returnsOnlyItemsOfThatUser() {
        cartItemRepository.save(CartItem.builder().user(user).book(book).quantity(1).build());
        cartItemRepository.save(CartItem.builder().user(otherUser).book(book).quantity(2).build());

        List<CartItem> result = cartItemRepository.findByUserId(user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void findByUserId_noItems_returnsEmptyList() {
        List<CartItem> result = cartItemRepository.findByUserId(user.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserId_multipleItemsSameUser_returnsAll() {
        cartItemRepository.save(CartItem.builder().user(user).book(book).quantity(1).build());
        cartItemRepository.save(CartItem.builder().user(user).book(otherBook).quantity(3).build());

        List<CartItem> result = cartItemRepository.findByUserId(user.getId());

        assertThat(result).hasSize(2);
    }

    // ── findByIdAndUserId ─────────────────────────────────────

    @Test
    void findByIdAndUserId_correctOwner_returnsItem() {
        CartItem saved = cartItemRepository.save(
                CartItem.builder().user(user).book(book).quantity(2).build());

        Optional<CartItem> result = cartItemRepository.findByIdAndUserId(saved.getId(), user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getQuantity()).isEqualTo(2);
    }

    @Test
    void findByIdAndUserId_wrongOwner_returnsEmpty() {
        CartItem saved = cartItemRepository.save(
                CartItem.builder().user(user).book(book).quantity(2).build());

        Optional<CartItem> result = cartItemRepository.findByIdAndUserId(saved.getId(), otherUser.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdAndUserId_unknownId_returnsEmpty() {
        Optional<CartItem> result = cartItemRepository.findByIdAndUserId(9999L, user.getId());

        assertThat(result).isEmpty();
    }

    // ── findByUserIdAndBookId ─────────────────────────────────

    @Test
    void findByUserIdAndBookId_matchingItem_returnsItem() {
        CartItem saved = cartItemRepository.save(
                CartItem.builder().user(user).book(book).quantity(1).build());

        Optional<CartItem> result = cartItemRepository.findByUserIdAndBookId(user.getId(), book.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void findByUserIdAndBookId_noMatchingItem_returnsEmpty() {
        Optional<CartItem> result = cartItemRepository.findByUserIdAndBookId(user.getId(), otherBook.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserIdAndBookId_wrongUser_returnsEmpty() {
        cartItemRepository.save(CartItem.builder().user(user).book(book).quantity(1).build());

        Optional<CartItem> result = cartItemRepository.findByUserIdAndBookId(otherUser.getId(), book.getId());

        assertThat(result).isEmpty();
    }
}
