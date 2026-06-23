package com.bnp.bookstore.mapper;


import com.bnp.bookstore.dto.response.BookResponse;
import com.bnp.bookstore.entities.Book;
import org.mapstruct.Mapper;


/** Converts a Book into the data object sent to the frontend. */


@Mapper(
        componentModel = "spring"
)


public interface BookMapper {


    BookResponse toResponse(Book book);


}
