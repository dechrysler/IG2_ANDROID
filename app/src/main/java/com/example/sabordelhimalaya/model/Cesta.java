package com.example.sabordelhimalaya.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Cesta {
    private final List<Object> elementos = new ArrayList<>();

    public void anadirPlato(Plato plato) {
        Objects.requireNonNull(plato);
        for (Object elemento : elementos) {
            if (elemento instanceof Plato && ((Plato) elemento).getNombre().equals(plato.getNombre())) {
                elementos.add(elemento);
                return;
            }
        }
        elementos.add(plato);
    }

    public void anadirMenuDiario(MenuDiario menuDiario) {
        elementos.add(Objects.requireNonNull(menuDiario));
    }

    public List<Object> getElementos() {
        return List.copyOf(elementos);
    }

    public int getNumeroElementos() {
        return elementos.size();
    }

    public Map<MenuDiario, Integer> getCantidadesMenus() {
        Map<MenuDiario, Integer> menus = new LinkedHashMap<>();
        for (Object elemento : elementos) {
            if (elemento instanceof MenuDiario) menus.merge((MenuDiario) elemento, 1, Integer::sum);
        }
        return menus;
    }

    public Map<Plato, Integer> getCantidadesPlatos() {
        Map<Plato, Integer> cantidades = new LinkedHashMap<>();
        for (Object elemento : elementos) {
            if (elemento instanceof Plato) {
                cantidades.merge((Plato) elemento, 1, Integer::sum);
            }
        }
        return cantidades;
    }

    public void cambiarCantidad(Plato plato, int cantidad) {
        cambiarCantidadElemento(plato, cantidad);
    }

    public void cambiarCantidad(MenuDiario menu, int cantidad) {
        cambiarCantidadElemento(menu, cantidad);
    }

    private void cambiarCantidadElemento(Object producto, int cantidad) {
        int posicion = elementos.indexOf(producto);
        if (posicion < 0) return;
        elementos.removeIf(elemento -> elemento == producto);
        for (int unidad = 0; unidad < cantidad; unidad++) {
            elementos.add(posicion + unidad, producto);
        }
    }
}
