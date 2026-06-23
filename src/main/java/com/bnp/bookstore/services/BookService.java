package com.bnp.bookstore.services;


import com.bnp.bookstore.dto.response.BookResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


/** Defines how to get books from the database. */
public interface BookService {

    /** Get one page of books. */
    Page<BookResponse> findALL(
            Pageable pageable
    );


}
