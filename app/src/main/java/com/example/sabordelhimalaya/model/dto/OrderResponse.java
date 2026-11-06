package com.example.sabordelhimalaya.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    public String id;
    public String numeroPedido;
    public String clienteId;
    public String clienteNombre;
    public String direccionEntrega;
    public String estado;
    public String cancelacion;
    public LocalDateTime fechaHora;
    public LocalDateTime fechaModificacion;
    public LocalDateTime fechaEntrega;
    public List<OrderItemResponse> items;
    public BigDecimal importeTotal;
    public String observacionesCancelacion;
    public boolean modificable;
    public boolean cancelable;
    public LocalDateTime estimacionLlegada;
}
