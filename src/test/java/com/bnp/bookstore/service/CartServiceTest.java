package com.bnp.bookstore.service;

import com.bnp.bookstore.dto.request.AddCartItemRequest;
import com.bnp.bookstore.dto.request.UpdateCartItemRequest;
import com.bnp.bookstore.dto.response.CartResponse;
import com.bnp.bookstore.entities.Book;
import com.bnp.bookstore.entities.CartItem;
import com.bnp.bookstore.entities.Role;
import com.bnp.bookstore.entities.User;
import com.bnp.bookstore.exception.BusinessException;
import com.bnp.bookstore.exception.ResourceNotFoundException;
import com.bnp.bookstore.repositories.BookRepository;
import com.bnp.bookstore.repositories.CartItemRepository;
import com.bnp.bookstore.repositories.UserRepository;
import com.bnp.bookstore.services.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    CartItemRepository cartRepository;
    @Mock
    BookRepository bookRepository;
    @Mock
    UserRepository userRepository;
    @InjectMocks
    CartServiceImpl cartService;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).email("user@test.com").role(Role.USER)
                .firstname("Test").lastname("User").build();

        book = Book.builder()
                .id(10L).title("Spring in Action").author("Craig Walls")
                .price(new BigDecimal("49.99")).stock(5).build();
    }

    // ── getCart ───────────────────────────────────────────────

    @Test
    void getCart_withItems_returnsTotalAndItems() {
        CartItem item = CartItem.builder()
                .id(1L).user(user).book(book).quantity(2).build();
        when(cartRepository.findByUserId(1L)).thenReturn(List.of(item));

        CartResponse response = cartService.getCart(1L);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().bookTitle()).isEqualTo("Spring in Action");
        assertThat(response.items().getFirst().quantity()).isEqualTo(2);
        assertThat(response.totalPrice()).isEqualByComparingTo("99.98");
    }

    @Test
    void getCart_emptyCart_returnsZeroTotal() {
        when(cartRepository.findByUserId(1L)).thenReturn(List.of());

        CartResponse response = cartService.getCart(1L);

        assertThat(response.items()).isEmpty();
        assertThat(response.totalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getCart_multipleItems_sumsTotal() {
        Book book2 = Book.builder()
                .id(11L).title("DDD").author("Evans")
                .price(new BigDecimal("30.00")).stock(3).build();
        CartItem i1 = CartItem.builder().id(1L).user(user).book(book).quantity(1).build();
        CartItem i2 = CartItem.builder().id(2L).user(user).book(book2).quantity(2).build();
        when(cartRepository.findByUserId(1L)).thenReturn(List.of(i1, i2));

        CartResponse response = cartService.getCart(1L);

        assertThat(response.items()).hasSize(2);
        // 49.99 * 1 + 30.00 * 2 = 109.99
        assertThat(response.totalPrice()).isEqualByComparingTo("109.99");
    }

    // ── addItem ───────────────────────────────────────────────

    @Test
    void addItem_newBook_savesNewCartItem() {
        AddCartItemRequest request = new AddCartItemRequest(10L, 2);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(cartRepository.findByUserIdAndBookId(1L, 10L)).thenReturn(Optional.empty());

        cartService.addItem(1L, request);

        verify(cartRepository).save(any(CartItem.class));
    }

    @Test
    void addItem_existingBook_incrementsQuantity() {
        CartItem existing = CartItem.builder()
                .id(1L).user(user).book(book).quantity(2).build();
        AddCartItemRequest request = new AddCartItemRequest(10L, 1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(cartRepository.findByUserIdAndBookId(1L, 10L)).thenReturn(Optional.of(existing));

        cartService.addItem(1L, request);

        assertThat(existing.getQuantity()).isEqualTo(3);
        verify(cartRepository).save(existing);
    }

    @Test
    void addItem_quantityExceedsStock_throwsBusinessException() {
        book.setStock(1);
        AddCartItemRequest request = new AddCartItemRequest(10L, 3);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> cartService.addItem(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Requested quantity exceeds available stock");

        verify(cartRepository, never()).save(any());
    }

    @Test
    void addItem_existingPlusNewExceedsStock_throwsBusinessException() {
        book.setStock(4);
        CartItem existing = CartItem.builder()
                .id(1L).user(user).book(book).quantity(3).build();
        AddCartItemRequest request = new AddCartItemRequest(10L, 2); // 3+2=5 > 4

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(cartRepository.findByUserIdAndBookId(1L, 10L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> cartService.addItem(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Requested quantity exceeds available stock");
    }

    @Test
    void addItem_bookNotFound_throwsResourceNotFoundException() {
        AddCartItemRequest request = new AddCartItemRequest(99L, 1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found");
    }

    @Test
    void addItem_userNotFound_throwsResourceNotFoundException() {
        AddCartItemRequest request = new AddCartItemRequest(10L, 1);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // ── updateQuantity ────────────────────────────────────────

    @Test
    void updateQuantity_validRequest_setsNewQuantity() {
        CartItem item = CartItem.builder()
                .id(1L).user(user).book(book).quantity(2).build();
        when(cartRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(item));

        cartService.updateQuantity(1L, 1L, new UpdateCartItemRequest(4));

        assertThat(item.getQuantity()).isEqualTo(4);
        verify(cartRepository).save(item);
    }

    @Test
    void updateQuantity_itemNotFound_throwsResourceNotFoundException() {
        when(cartRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateQuantity(1L, 99L, new UpdateCartItemRequest(2)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cart item not found");
    }

    @Test
    void updateQuantity_exceedsStock_throwsBusinessException() {
        book.setStock(3);
        CartItem item = CartItem.builder()
                .id(1L).user(user).book(book).quantity(1).build();
        when(cartRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> cartService.updateQuantity(1L, 1L, new UpdateCartItemRequest(5)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Requested quantity exceeds available stock");

        verify(cartRepository, never()).save(any());
    }

    // ── removeItem ────────────────────────────────────────────

    @Test
    void removeItem_existingItem_deletesFromRepository() {
        CartItem item = CartItem.builder()
                .id(1L).user(user).book(book).quantity(1).build();
        when(cartRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(item));

        cartService.removeItem(1L, 1L);

        verify(cartRepository).delete(item);
    }

    @Test
    void removeItem_itemNotFound_throwsResourceNotFoundException() {
        when(cartRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeItem(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cart item not found");

        verify(cartRepository, never()).delete(any());
    }
}
