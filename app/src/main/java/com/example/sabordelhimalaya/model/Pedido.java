package com.example.sabordelhimalaya.model;

import java.time.LocalDateTime;
import java.util.List;

public class Pedido {
    private final int idPedido;
    private int idUsuario;
    private final LocalDateTime fecha;
    private final List<Plato> platos;
    private final List<MenuDiario> menusDiarios;
    private final int numeroElementos;
    private final double precioTotal;
    private final EstadoPedidos estadoPedido;

    public Pedido(int idPedido, LocalDateTime fecha, List<Plato> platos, List<MenuDiario> menusDiarios,
                  int numeroElementos, double precioTotal, EstadoPedidos estadoPedido) {
        this.idPedido = idPedido;
        this.fecha = fecha;
        this.platos = List.copyOf(platos);
        this.menusDiarios = List.copyOf(menusDiarios);
        this.numeroElementos = numeroElementos;
        this.precioTotal = precioTotal;
        this.estadoPedido = estadoPedido;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public List<Plato> getPlatos() {
        return platos;
    }

    public List<MenuDiario> getMenusDiarios() {
        return menusDiarios;
    }

    public int getNumeroElementos() {
        return numeroElementos;
    }

    public double getPrecioTotal() {
        return precioTotal;
    }

    public EstadoPedidos getEstadoPedido() {
        return estadoPedido;
    }
}
