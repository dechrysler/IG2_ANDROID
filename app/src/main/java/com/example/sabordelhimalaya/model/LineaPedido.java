package com.example.sabordelhimalaya.model;

import com.example.sabordelhimalaya.model.dto.OrderItemResponse;

import java.math.BigDecimal;
import java.util.List;

/** Línea (producto o menú) de un pedido. */
public class LineaPedido {
    private final String tipo;
    private final String platoId;
    private final String menuId;
    private final String nombre;
    private final List<String> componentes;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal importeLinea;

    public LineaPedido(String tipo, String platoId, String menuId, String nombre,
                       List<String> componentes, int cantidad,
                       BigDecimal precioUnitario, BigDecimal importeLinea) {
        this.tipo = tipo;
        this.platoId = platoId;
        this.menuId = menuId;
        this.nombre = nombre;
        this.componentes = componentes == null ? List.of() : List.copyOf(componentes);
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.importeLinea = importeLinea;
    }

    public static LineaPedido from(OrderItemResponse response) {
        return new LineaPedido(response.tipo, response.platoId, response.menuId,
                response.nombre, response.componentes, response.cantidad,
                response.precioUnitario, response.importeLinea);
    }

    public String getTipo() {
        return tipo;
    }

    public String getPlatoId() {
        return platoId;
    }

    public String getMenuId() {
        return menuId;
    }

    public String getNombre() {
        return nombre;
    }

    public List<String> getComponentes() {
        return componentes;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getImporteLinea() {
        return importeLinea;
    }
}
