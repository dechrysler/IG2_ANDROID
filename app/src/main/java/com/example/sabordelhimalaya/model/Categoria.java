package com.example.sabordelhimalaya.model;

import com.example.sabordelhimalaya.model.dto.CategoryResponse;

import java.util.Locale;

/** Categoría de la carta tal y como la devuelve el backend (10 categorías fijas). */
public class Categoria {
    private final int numero;
    private final String nombre;
    private final String descripcion;

    public Categoria(int numero, String nombre, String descripcion) {
        this.numero = numero;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public static Categoria from(CategoryResponse response) {
        return new Categoria(response.numero, response.nombre, response.descripcion);
    }

    public int getNumero() {
        return numero;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** Texto legible para la pestaña: ENTRANTES -> Entrantes. */
    public String getNombreVisible() {
        if (nombre == null || nombre.isEmpty()) {
            return descripcion != null ? descripcion : "";
        }
        String texto = nombre.replace('_', ' ').toLowerCase(Locale.forLanguageTag("es-ES"));
        return texto.substring(0, 1).toUpperCase(Locale.forLanguageTag("es-ES")) + texto.substring(1);
    }
}
