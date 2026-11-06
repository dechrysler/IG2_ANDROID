package com.example.sabordelhimalaya.model.dto;

import java.util.List;

public class CreateOrderRequest {
    public final List<OrderItemRequest> items;
    public final String direccionEntrega;

    public CreateOrderRequest(List<OrderItemRequest> items, String direccionEntrega) {
        this.items = items;
        this.direccionEntrega = direccionEntrega;
    }
}
