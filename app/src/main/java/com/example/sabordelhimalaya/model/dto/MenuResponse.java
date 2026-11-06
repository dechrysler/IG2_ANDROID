package com.example.sabordelhimalaya.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class MenuResponse {
    public String id;
    public String nombre;
    public String descripcion;
    /** Campo heredado; la app usa las cuatro secciones. */
    public List<DishResponse> productos;
    public BigDecimal precio;
    public BigDecimal precioPersonalizado;
    public boolean disponible;
    public boolean esMenuDelDia;
    public LocalDate fechaMenuDelDia;
    public List<DishResponse> entrantes;
    public List<DishResponse> principales;
    public List<DishResponse> postres;
    public List<DishResponse> bebidas;
    public LocalDateTime creadoEn;
}
