package com.bnp.bookstore.controllers;


import com.bnp.bookstore.dto.response.BookResponse;
import com.bnp.bookstore.services.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/** Handles HTTP requests to browse the book catalogue. No login needed. */
@Slf4j
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(
        name = "Books",
        description = "Book catalog management"
)
public class BookController {


    private final BookService bookService;


    /** Get a page of books from the catalogue. */
    @GetMapping
    @Operation(
            summary = "Get all books"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of books returned successfully")
    })
    public ResponseEntity<Page<BookResponse>> findAll(@PageableDefault(size = 12, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(bookService.findALL(pageable));
    }


}
