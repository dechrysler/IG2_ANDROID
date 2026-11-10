package com.example.sabordelhimalaya.model;

import com.example.sabordelhimalaya.model.dto.OrderItemResponse;

import java.math.BigDecimal;
import java.util.List;

public class LineaPedido {
    private final String tipo;
    private final String platoId;
    private final String menuId;
    private final String nombre;
    private final List<String> componentes;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal importeLinea;
    private final SeleccionMenu seleccion;

    public LineaPedido(String tipo, String platoId, String menuId, String nombre,
                       List<String> componentes, int cantidad,
                       BigDecimal precioUnitario, BigDecimal importeLinea,
                       SeleccionMenu seleccion) {
        this.tipo = tipo;
        this.platoId = platoId;
        this.menuId = menuId;
        this.nombre = nombre;
        this.componentes = componentes == null ? List.of() : List.copyOf(componentes);
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.importeLinea = importeLinea;
        this.seleccion = seleccion;
    }

    public static LineaPedido from(OrderItemResponse response) {
        return new LineaPedido(response.tipo, response.platoId, response.menuId,
                response.nombre, response.componentes, response.cantidad,
                response.precioUnitario, response.importeLinea,
                seleccionDe(response.seleccion));
    }

    private static SeleccionMenu seleccionDe(OrderItemResponse.Seleccion seleccion) {
        if (seleccion == null) {
            return null;
        }
        return SeleccionMenu.from(
                opcion(seleccion.entrante),
                opcion(seleccion.principal),
                opcion(seleccion.postre),
                opcion(seleccion.bebida)
        );
    }

    private static OpcionSeleccionada opcion(OrderItemResponse.Opcion opcion) {
        if (opcion == null) {
            return null;
        }
        return new OpcionSeleccionada(opcion.id, opcion.nombre);
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

    public SeleccionMenu getSeleccion() {
        return seleccion;
    }
}
