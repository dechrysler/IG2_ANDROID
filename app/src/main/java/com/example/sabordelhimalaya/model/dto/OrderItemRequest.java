package com.example.sabordelhimalaya.model.dto;

public class OrderItemRequest {
    public final String platoId;
    public final String menuId;
    public final int cantidad;

    private OrderItemRequest(String platoId, String menuId, int cantidad) {
        this.platoId = platoId;
        this.menuId = menuId;
        this.cantidad = cantidad;
    }

    public static OrderItemRequest producto(String platoId, int cantidad) {
        return new OrderItemRequest(platoId, null, cantidad);
    }

    public static OrderItemRequest menu(String menuId, int cantidad) {
        return new OrderItemRequest(null, menuId, cantidad);
    }
}
