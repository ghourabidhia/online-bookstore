package com.bnp.bookstore.repository;


import com.bnp.bookstore.entities.Book;
import com.bnp.bookstore.repositories.BookRepository;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;


import java.math.BigDecimal;


import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
class BookRepositoryTest {


    @Autowired
    private BookRepository bookRepository;


    @Test
    void shouldSaveAndFindBook() {


        Book book =
                Book.builder()
                        .title("Clean Code")
                        .author("Robert Martin")
                        .price(
                                new BigDecimal("45.90")
                        )
                        .stock(20)
                        .build();


        Book saved =
                bookRepository.save(book);


        Book result =
                bookRepository
                        .findById(saved.getId())
                        .orElseThrow();


        assertThat(result.getTitle())
                .isEqualTo("Clean Code");


        assertThat(result.getPrice())
                .isEqualByComparingTo(
                        "45.90"
                );

    }

}
