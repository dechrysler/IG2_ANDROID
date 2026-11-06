package com.example.sabordelhimalaya.model.dto;

public class OrderItemRequest {
    public final String platoId;
    public final String menuId;
    public final String entranteId;
    public final String principalId;
    public final String postreId;
    public final String bebidaId;
    public final int cantidad;

    private OrderItemRequest(String platoId, String menuId, String entranteId,
                             String principalId, String postreId, String bebidaId,
                             int cantidad) {
        this.platoId = platoId;
        this.menuId = menuId;
        this.entranteId = entranteId;
        this.principalId = principalId;
        this.postreId = postreId;
        this.bebidaId = bebidaId;
        this.cantidad = cantidad;
    }

    public static OrderItemRequest producto(String platoId, int cantidad) {
        return new OrderItemRequest(platoId, null, null, null, null, null, cantidad);
    }

    public static OrderItemRequest menu(String menuId, String entranteId,
                                        String principalId, String postreId,
                                        String bebidaId, int cantidad) {
        return new OrderItemRequest(null, menuId, entranteId, principalId,
                postreId, bebidaId, cantidad);
    }
}
