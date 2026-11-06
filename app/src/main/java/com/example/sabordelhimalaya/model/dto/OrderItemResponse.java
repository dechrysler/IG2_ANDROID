package com.example.sabordelhimalaya.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderItemResponse {
    public String tipo;
    public String platoId;
    public String menuId;
    public String nombre;
    public List<String> componentes;
    public int cantidad;
    public BigDecimal precioUnitario;
    public BigDecimal importeLinea;
}
