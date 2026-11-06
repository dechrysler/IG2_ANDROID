package com.example.sabordelhimalaya.model.dto;

/** Respuesta común de registro, activación, login y reenvío de código. */
public class AuthResponse {
    public String mensaje;
    public String token;
    public String usuarioId;
    public String nombre;
    public String correoElectronico;
    public String rol;
    public String direccion;
}
