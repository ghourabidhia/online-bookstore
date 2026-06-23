package com.bnp.bookstore.services.impl;


import com.bnp.bookstore.dto.response.BookResponse;
import com.bnp.bookstore.mapper.BookMapper;

import com.bnp.bookstore.repositories.BookRepository;
import com.bnp.bookstore.services.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Gets books from the database. Read-only — nothing is saved or changed here.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {


    private final BookRepository bookRepository;

    private final BookMapper bookMapper;


    /**
     * Return one page of books from the catalogue.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> findALL(Pageable pageable) {
        log.debug("Fetching book catalogue — page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<BookResponse> result = bookRepository.findAll(pageable).map(bookMapper::toResponse);

        log.debug("Returning {} book(s) (total elements: {})", result.getNumberOfElements(), result.getTotalElements());
        return result;
    }


}
