package com.example.sabordelhimalaya.model.dto;

import java.math.BigDecimal;

public class DishResponse {
    public String id;
    public String nombre;
    public String descripcion;
    public String imagenUrl;
    public Integer categoria;
    public String categoriaNombre;
    public BigDecimal precio;
    public boolean disponible;
    public Boolean enCarta;
}
