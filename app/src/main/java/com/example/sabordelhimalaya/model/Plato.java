package com.example.sabordelhimalaya.model;

public class Plato {
    private final String nombre;
    private final Categoria categoria;
    private final String descripcion;
    private final double precio;

    public Plato(String nombre, Categoria categoria, String descripcion, double precio) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    public String getNombre() {
        return nombre;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPrecio() {
        return precio;
    }
}
