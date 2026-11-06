package com.example.sabordelhimalaya.model;

import com.example.sabordelhimalaya.model.dto.DishResponse;
import com.example.sabordelhimalaya.model.dto.MenuResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Menú del día: colección libre de productos, no una estructura fija. */
public class Menu {
    private final String id;
    private final String nombre;
    private final String descripcion;
    private final List<Plato> productos;
    private final BigDecimal precio;
    private final boolean disponible;
    private final LocalDate fechaMenuDelDia;

    public Menu(String id, String nombre, String descripcion, List<Plato> productos,
                BigDecimal precio, boolean disponible, LocalDate fechaMenuDelDia) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.productos = List.copyOf(productos);
        this.precio = precio;
        this.disponible = disponible;
        this.fechaMenuDelDia = fechaMenuDelDia;
    }

    public static Menu from(MenuResponse response) {
        List<Plato> productos = new ArrayList<>();
        if (response.productos != null) {
            for (DishResponse dish : response.productos) {
                productos.add(Plato.from(dish));
            }
        }
        return new Menu(response.id, response.nombre, response.descripcion, productos,
                response.precio, response.disponible, response.fechaMenuDelDia);
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

    public List<Plato> getProductos() {
        return productos;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public LocalDate getFechaMenuDelDia() {
        return fechaMenuDelDia;
    }

    public String nombresProductos() {
        StringBuilder builder = new StringBuilder();
        for (Plato plato : productos) {
            if (builder.length() > 0) {
                builder.append(" · ");
            }
            builder.append(plato.getNombre());
        }
        return builder.toString();
    }
}
