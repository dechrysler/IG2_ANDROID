package com.example.sabordelhimalaya.model;

import com.example.sabordelhimalaya.model.dto.DishResponse;
import com.example.sabordelhimalaya.model.dto.MenuResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Menú diario estructurado: entrantes, principales, postres y bebidas. */
public class Menu {
    private final String id;
    private final String nombre;
    private final String descripcion;
    private final List<Plato> entrantes;
    private final List<Plato> principales;
    private final List<Plato> postres;
    private final List<Plato> bebidas;
    private final BigDecimal precio;
    private final boolean disponible;
    private final LocalDate fechaMenuDelDia;

    public Menu(String id, String nombre, String descripcion,
                List<Plato> entrantes, List<Plato> principales,
                List<Plato> postres, List<Plato> bebidas,
                BigDecimal precio, boolean disponible, LocalDate fechaMenuDelDia) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.entrantes = List.copyOf(entrantes);
        this.principales = List.copyOf(principales);
        this.postres = List.copyOf(postres);
        this.bebidas = List.copyOf(bebidas);
        this.precio = precio;
        this.disponible = disponible;
        this.fechaMenuDelDia = fechaMenuDelDia;
    }

    public static Menu from(MenuResponse response) {
        return new Menu(response.id, response.nombre, response.descripcion,
                platos(response.entrantes),
                platos(response.principales),
                platos(response.postres),
                platos(response.bebidas),
                response.precio, response.disponible, response.fechaMenuDelDia);
    }

    private static List<Plato> platos(List<DishResponse> dishes) {
        List<Plato> platos = new ArrayList<>();
        if (dishes != null) {
            for (DishResponse dish : dishes) {
                platos.add(Plato.from(dish));
            }
        }
        return platos;
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

    public List<Plato> getEntrantes() {
        return entrantes;
    }

    public List<Plato> getPrincipales() {
        return principales;
    }

    public List<Plato> getPostres() {
        return postres;
    }

    public List<Plato> getBebidas() {
        return bebidas;
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

    /** Todos los platos, útil para imágenes y resúmenes. */
    public List<Plato> getTodosLosPlatos() {
        List<Plato> todos = new ArrayList<>();
        todos.addAll(entrantes);
        todos.addAll(principales);
        todos.addAll(postres);
        todos.addAll(bebidas);
        return todos;
    }

    public boolean estaCompleto() {
        return entrantes.size() == 3 && principales.size() == 3
                && postres.size() == 2 && bebidas.size() == 1;
    }
}
