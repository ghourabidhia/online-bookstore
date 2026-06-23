package com.bnp.bookstore.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


/**
 * One book inside an order.
 * The price is saved at checkout, so old orders are never affected by price changes.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {


    @Id
    @GeneratedValue(strategy =
            GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(
            fetch = FetchType.LAZY
    )
    private Order order;


    @ManyToOne(
            fetch = FetchType.LAZY
    )
    private Book book;


    private Integer quantity;


    private BigDecimal price;


}