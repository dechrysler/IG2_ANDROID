package com.example.sabordelhimalaya.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class HacerPedido {
    private final int idUsuario;
    private final LocalDateTime fecha;
    private final Object[] items;
    private final int numeroTotalItems;
    private final double precioTotal;

    public HacerPedido(int idUsuario, Cesta cesta) {
        this.idUsuario = idUsuario;
        this.fecha = LocalDateTime.now();
        this.items = cesta.getElementos().toArray();
        this.numeroTotalItems = items.length;

        BigDecimal total = BigDecimal.ZERO;
        for (Object item : items) {
            double precio = item instanceof Plato
                    ? ((Plato) item).getPrecio()
                    : ((MenuDiario) item).getPrecio();
            total = total.add(BigDecimal.valueOf(precio));
        }
        this.precioTotal = total.doubleValue();
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Object[] getItems() {
        return items.clone();
    }

    public int getNumeroTotalItems() {
        return numeroTotalItems;
    }

    public double getPrecioTotal() {
        return precioTotal;
    }
}
