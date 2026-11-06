package com.example.sabordelhimalaya.model;

/** Opción elegida dentro de una sección del menú. */
public class OpcionSeleccionada {
    private final String id;
    private final String nombre;

    public OpcionSeleccionada(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean valida() {
        return id != null && !id.isEmpty();
    }
}
