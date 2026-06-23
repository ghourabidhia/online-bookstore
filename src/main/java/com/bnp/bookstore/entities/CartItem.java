package com.bnp.bookstore.entities;


import jakarta.persistence.*;
import lombok.*;


/** One book in a user's shopping cart, with a quantity. */
@Entity
@Table(name = "cart_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    private Book book;


    private Integer quantity;


}