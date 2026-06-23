package com.bnp.bookstore.mapper;


import com.bnp.bookstore.dto.response.OrderItemResponse;
import com.bnp.bookstore.dto.response.OrderResponse;


import com.bnp.bookstore.entities.Order;
import com.bnp.bookstore.entities.OrderItem;
import org.mapstruct.Mapper;


import java.util.List;


/** Converts an Order and its items into the data objects sent to the frontend. */
@Mapper(
        componentModel = "spring"
)
public interface OrderMapper {


    default OrderResponse toResponse(Order order) {

        List<OrderItemResponse> items =
                order.getItems()
                        .stream()
                        .map(this::mapItem)
                        .toList();


        return new OrderResponse(
                order.getId(),
                order.getTotalPrice(),
                order.getOrderDate(),
                items
        );

    }


    default OrderItemResponse mapItem(OrderItem item) {

        return new OrderItemResponse(
                item.getBook().getTitle(),
                item.getQuantity(),
                item.getPrice());

    }


}