package com.example.sabordelhimalaya.model;

import java.time.LocalDate;
import java.util.List;

public class MenuDiario {
    private final LocalDate fecha;
    private final List<Plato> entrantes;
    private final List<Plato> principales;
    private final List<Plato> postres;
    private final List<Plato> bebidas;
    private final double precio;

    public MenuDiario(LocalDate fecha, List<Plato> entrantes, List<Plato> principales,
                      List<Plato> postres, List<Plato> bebidas, double precio) {
        this.fecha = fecha;
        this.entrantes = List.copyOf(entrantes);
        this.principales = List.copyOf(principales);
        this.postres = List.copyOf(postres);
        this.bebidas = List.copyOf(bebidas);
        this.precio = precio;
    }

    public LocalDate getFecha() {
        return fecha;
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

    public double getPrecio() {
        return precio;
    }
}
