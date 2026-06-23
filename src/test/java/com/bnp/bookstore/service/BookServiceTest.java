package com.bnp.bookstore.service;


import com.bnp.bookstore.dto.response.BookResponse;
import com.bnp.bookstore.entities.Book;
import com.bnp.bookstore.mapper.BookMapper;
import com.bnp.bookstore.repositories.BookRepository;
import com.bnp.bookstore.services.impl.BookServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    void shouldReturnBooksPage() {

        Book book =
                Book.builder()
                        .id(1L)
                        .title("Clean Code")
                        .author("Martin")
                        .price(new BigDecimal("45.90"))
                        .stock(10)
                        .build();

        BookResponse response =
                new BookResponse(
                        1L,
                        "Clean Code",
                        "Martin",
                        new BigDecimal("45.90"),
                        10
                );

        when(bookRepository.findAll(any(PageRequest.class)))
                .thenReturn(
                        new PageImpl<>(List.of(book))
                );

        when(bookMapper.toResponse(book))
                .thenReturn(response);

        Page<BookResponse> result =
                bookService.findALL(
                        PageRequest.of(0, 10)
                );

        assertThat(result.getContent())
                .hasSize(1);

        assertThat(result.getContent().getFirst().title())
                .isEqualTo("Clean Code");

        verify(bookRepository)
                .findAll(any(PageRequest.class));

    }


}