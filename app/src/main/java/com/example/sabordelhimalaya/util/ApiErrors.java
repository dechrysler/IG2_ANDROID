package com.example.sabordelhimalaya.util;

import com.example.sabordelhimalaya.model.dto.ApiError;
import com.example.sabordelhimalaya.model.remote.ApiClient;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;

import retrofit2.Response;

/** Interpreta los errores del backend y los traduce a mensajes de usuario. */
public final class ApiErrors {

    private ApiErrors() {
    }

    public static ApiError parse(Response<?> response) {
        if (response == null || response.errorBody() == null) {
            return null;
        }
        try {
            String body = response.errorBody().string();
            if (body == null || body.isEmpty()) {
                return null;
            }
            return ApiClient.gson().fromJson(body, ApiError.class);
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    public static String message(Response<?> response) {
        ApiError error = parse(response);
        if (error != null && error.mensaje != null && !error.mensaje.isEmpty()) {
            return error.mensaje;
        }
        if (response != null) {
            switch (response.code()) {
                case 400:
                    return "No se pudo completar la operación.";
                case 401:
                    return "La sesión ha caducado. Vuelve a iniciar sesión.";
                case 403:
                    return "No tienes permisos para esta operación.";
                case 404:
                    return "No se ha encontrado el recurso solicitado.";
                case 409:
                    return "Hay un conflicto con los datos. Inténtalo de nuevo.";
                case 500:
                    return "El servidor ha tenido un problema. Inténtalo más tarde.";
                default:
                    return "Error del servidor (" + response.code() + ").";
            }
        }
        return "Ha ocurrido un error inesperado.";
    }

    public static Map<String, String> fieldErrors(Response<?> response) {
        ApiError error = parse(response);
        if (error == null || error.errores == null) {
            return Collections.emptyMap();
        }
        return error.errores;
    }

    public static String networkMessage(Throwable throwable) {
        Throwable cause = throwable;
        while (cause != null && cause.getCause() != null && !(cause instanceof ConnectException)
                && !(cause instanceof SocketTimeoutException)
                && !(cause instanceof UnknownHostException)) {
            cause = cause.getCause();
        }
        if (cause instanceof SocketTimeoutException) {
            return "El servidor tarda demasiado en responder. Comprueba la conexión.";
        }
        if (cause instanceof UnknownHostException) {
            return "No se encuentra el servidor. Comprueba la dirección de la API.";
        }
        if (cause instanceof ConnectException) {
            return "No se puede conectar con el servidor. Comprueba que el backend esté arrancado.";
        }
        return "Sin conexión. Revisa tu red e inténtalo de nuevo.";
    }
}
