package com.example.sabordelhimalaya.util;

import android.content.Context;

import com.example.sabordelhimalaya.R;

import retrofit2.Response;

/** Reglas locales obligatorias sobre roles y sesión. */
public final class Roles {
    public static final String CLIENTE = "CLIENTE";

    private Roles() {
    }

    public static boolean esCliente(String rol) {
        return CLIENTE.equals(rol);
    }

    public static String mensajeSoloClientes(Context context) {
        return context.getString(R.string.error_solo_clientes);
    }

    public static boolean esNoAutorizado(Response<?> response) {
        return response != null && response.code() == 401;
    }
}
