package com.bnp.bookstore.controller;

import com.bnp.bookstore.dto.response.BookResponse;
import com.bnp.bookstore.services.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class BookControllerTest {

    @Autowired WebApplicationContext context;
    @MockitoBean BookService bookService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void findAll_returnsPageOfBooks() throws Exception {
        BookResponse book = new BookResponse(1L, "Clean Code", "Martin", new BigDecimal("45.90"), 10);
        when(bookService.findALL(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(book)));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Clean Code"))
                .andExpect(jsonPath("$.content[0].author").value("Martin"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void findAll_emptyPage_returns200WithEmptyContent() throws Exception {
        when(bookService.findALL(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void findAll_withPaginationParams_returns200() throws Exception {
        BookResponse book = new BookResponse(2L, "DDD", "Evans", new BigDecimal("55.00"), 5);
        when(bookService.findALL(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(book)));

        mockMvc.perform(get("/api/books").param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2));
    }

    @Test
    void findAll_noAuthRequired_publicEndpoint() throws Exception {
        when(bookService.findALL(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        // no authentication header or security context — must still return 200
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk());
    }
}
