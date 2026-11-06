package com.example.sabordelhimalaya.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatos es-ES para importes, fechas y etiquetas de estado. */
public final class Formatters {
    private static final Locale ES = Locale.forLanguageTag("es-ES");
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", ES);
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("HH:mm", ES);
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", ES);

    private Formatters() {
    }

    public static String money(BigDecimal value) {
        if (value == null) {
            return "—";
        }
        NumberFormat format = NumberFormat.getCurrencyInstance(ES);
        return format.format(value);
    }

    public static String dateTime(LocalDateTime value) {
        return value == null ? "—" : DATE_TIME.format(value);
    }

    public static String time(LocalDateTime value) {
        return value == null ? "—" : TIME.format(value);
    }

    public static String date(LocalDate value) {
        return value == null ? "—" : DATE.format(value);
    }

    public static String estado(String estado) {
        if (estado == null) {
            return "";
        }
        switch (estado) {
            case "RECIBIDO":
                return "Recibido";
            case "EN_PREPARACION":
                return "En preparación";
            case "LISTO":
                return "Listo";
            case "ENTREGADO":
                return "Entregado";
            default:
                return estado;
        }
    }

    public static int estadoPaso(String estado) {
        if (estado == null) {
            return 0;
        }
        switch (estado) {
            case "RECIBIDO":
                return 0;
            case "EN_PREPARACION":
                return 1;
            case "LISTO":
                return 2;
            case "ENTREGADO":
                return 3;
            default:
                return 0;
        }
    }

    public static String cancelacion(String cancelacion) {
        if (cancelacion == null || "SIN_CANCELAR".equals(cancelacion)) {
            return null;
        }
        if ("CANCELADO_SIN_CARGO".equals(cancelacion)) {
            return "Cancelado sin cargo";
        }
        if ("CANCELADO_CON_CARGO".equals(cancelacion)) {
            return "Cancelado con cargo (100 %)";
        }
        return cancelacion;
    }

    public static boolean estaCancelado(String cancelacion) {
        return cancelacion != null && !"SIN_CANCELAR".equals(cancelacion);
    }

    public static String tipoLinea(String tipo) {
        return "MENU".equals(tipo) ? "Menú" : "Producto";
    }
}
