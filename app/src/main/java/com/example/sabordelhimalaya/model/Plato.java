package com.example.sabordelhimalaya.model;

import com.example.sabordelhimalaya.model.dto.DishResponse;

import java.math.BigDecimal;

/** Producto de la carta. */
public class Plato {
    private final String id;
    private final String nombre;
    private final String descripcion;
    private final String imagenUrl;
    private final int categoria;
    private final String categoriaNombre;
    private final BigDecimal precio;
    private final boolean disponible;

    public Plato(String id, String nombre, String descripcion, String imagenUrl,
                 int categoria, String categoriaNombre, BigDecimal precio, boolean disponible) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.imagenUrl = imagenUrl;
        this.categoria = categoria;
        this.categoriaNombre = categoriaNombre;
        this.precio = precio;
        this.disponible = disponible;
    }

    public static Plato from(DishResponse response) {
        return new Plato(
                response.id,
                response.nombre,
                response.descripcion,
                response.imagenUrl,
                response.categoria != null ? response.categoria : 0,
                response.categoriaNombre,
                response.precio,
                response.disponible
        );
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public int getCategoria() {
        return categoria;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public boolean isDisponible() {
        return disponible;
    }
}
