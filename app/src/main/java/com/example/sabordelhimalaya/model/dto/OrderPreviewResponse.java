package com.example.sabordelhimalaya.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderPreviewResponse {
    public String clienteId;
    public String direccionEntrega;
    public List<OrderItemResponse> items;
    public BigDecimal importeTotal;
}
