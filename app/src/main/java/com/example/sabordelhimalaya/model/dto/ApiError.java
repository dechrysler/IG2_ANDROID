package com.example.sabordelhimalaya.model.dto;

import java.util.Map;

/** Cuerpo de error estándar del backend. */
public class ApiError {
    public String codigo;
    public String mensaje;
    public Map<String, String> errores;
}
