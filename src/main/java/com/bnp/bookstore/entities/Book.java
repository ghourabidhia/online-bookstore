package com.bnp.bookstore.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/** A book in the store. Saved in the database with its price and stock count. */
@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    /** Title of the book. */
    @Column(nullable = false)
    private String title;

    /** Name of the author. */
    @Column(nullable = false)
    private String author;

    /** Price of the book. */
    @Column(nullable = false)
    private BigDecimal price;

    /** How many copies are available to buy. */
    @Column(nullable = false)
    private Integer stock;
}
