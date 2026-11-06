package com.example.sabordelhimalaya.model;

import com.example.sabordelhimalaya.model.dto.OrderItemResponse;
import com.example.sabordelhimalaya.model.dto.OrderResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Pedido del cliente tal y como lo devuelve el backend. */
public class Pedido {
    private final String id;
    private final String numeroPedido;
    private final String clienteId;
    private final String clienteNombre;
    private final String direccionEntrega;
    private final String estado;
    private final String cancelacion;
    private final LocalDateTime fechaHora;
    private final LocalDateTime fechaModificacion;
    private final LocalDateTime fechaEntrega;
    private final List<LineaPedido> items;
    private final BigDecimal importeTotal;
    private final String observacionesCancelacion;
    private final boolean modificable;
    private final boolean cancelable;
    private final LocalDateTime estimacionLlegada;

    public Pedido(String id, String numeroPedido, String clienteId, String clienteNombre,
                  String direccionEntrega, String estado, String cancelacion,
                  LocalDateTime fechaHora, LocalDateTime fechaModificacion,
                  LocalDateTime fechaEntrega, List<LineaPedido> items,
                  BigDecimal importeTotal, String observacionesCancelacion,
                  boolean modificable, boolean cancelable, LocalDateTime estimacionLlegada) {
        this.id = id;
        this.numeroPedido = numeroPedido;
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
        this.direccionEntrega = direccionEntrega;
        this.estado = estado;
        this.cancelacion = cancelacion;
        this.fechaHora = fechaHora;
        this.fechaModificacion = fechaModificacion;
        this.fechaEntrega = fechaEntrega;
        this.items = List.copyOf(items);
        this.importeTotal = importeTotal;
        this.observacionesCancelacion = observacionesCancelacion;
        this.modificable = modificable;
        this.cancelable = cancelable;
        this.estimacionLlegada = estimacionLlegada;
    }

    public static Pedido from(OrderResponse response) {
        List<LineaPedido> lineas = new ArrayList<>();
        if (response.items != null) {
            for (OrderItemResponse item : response.items) {
                lineas.add(LineaPedido.from(item));
            }
        }
        return new Pedido(response.id, response.numeroPedido, response.clienteId,
                response.clienteNombre, response.direccionEntrega, response.estado,
                response.cancelacion, response.fechaHora, response.fechaModificacion,
                response.fechaEntrega, lineas, response.importeTotal,
                response.observacionesCancelacion, response.modificable,
                response.cancelable, response.estimacionLlegada);
    }

    public String getId() {
        return id;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public String getClienteId() {
        return clienteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public String getEstado() {
        return estado;
    }

    public String getCancelacion() {
        return cancelacion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public List<LineaPedido> getItems() {
        return items;
    }

    public BigDecimal getImporteTotal() {
        return importeTotal;
    }

    public String getObservacionesCancelacion() {
        return observacionesCancelacion;
    }

    public boolean isModificable() {
        return modificable;
    }

    public boolean isCancelable() {
        return cancelable;
    }

    public LocalDateTime getEstimacionLlegada() {
        return estimacionLlegada;
    }
}
