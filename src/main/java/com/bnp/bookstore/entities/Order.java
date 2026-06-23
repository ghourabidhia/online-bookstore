package com.bnp.bookstore.entities;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/** A customer's order. Created from their shopping cart. */
@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    private User user;


    @Column(nullable = false)
    private BigDecimal totalPrice;


    @Column(nullable = false)
    private LocalDateTime orderDate;


    /**
     * The books inside this order.
     * Saving or deleting the order also saves or deletes these items automatically.
     */
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<OrderItem> items =
            new ArrayList<>();


    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }


}