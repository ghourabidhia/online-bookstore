package com.bnp.bookstore.mapper;


import com.bnp.bookstore.dto.request.RegisterRequest;
import com.bnp.bookstore.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


/** Converts a registration request into a User entity. */


@Mapper(
        componentModel = "spring"
)


public interface UserMapper {


    @Mapping(
            target = "password",
            ignore = true
    )
    User toEntity(RegisterRequest request);


}
