package com.example.sabordelhimalaya.model;

/** Selección exacta de un menú: un entrante, un principal, un postre y una bebida. */
public class SeleccionMenu {
    private final OpcionSeleccionada entrante;
    private final OpcionSeleccionada principal;
    private final OpcionSeleccionada postre;
    private final OpcionSeleccionada bebida;

    public SeleccionMenu(OpcionSeleccionada entrante, OpcionSeleccionada principal,
                         OpcionSeleccionada postre, OpcionSeleccionada bebida) {
        this.entrante = entrante;
        this.principal = principal;
        this.postre = postre;
        this.bebida = bebida;
    }

    public static SeleccionMenu from(OpcionSeleccionada entrante, OpcionSeleccionada principal,
                                     OpcionSeleccionada postre, OpcionSeleccionada bebida) {
        if (entrante == null && principal == null && postre == null && bebida == null) {
            return null;
        }
        return new SeleccionMenu(entrante, principal, postre, bebida);
    }

    public OpcionSeleccionada getEntrante() {
        return entrante;
    }

    public OpcionSeleccionada getPrincipal() {
        return principal;
    }

    public OpcionSeleccionada getPostre() {
        return postre;
    }

    public OpcionSeleccionada getBebida() {
        return bebida;
    }

    public boolean estaCompleta() {
        return entrante != null && entrante.valida()
                && principal != null && principal.valida()
                && postre != null && postre.valida()
                && bebida != null && bebida.valida();
    }
}
