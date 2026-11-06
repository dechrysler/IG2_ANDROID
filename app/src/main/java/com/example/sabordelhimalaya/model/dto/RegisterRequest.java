package com.example.sabordelhimalaya.model.dto;

public class RegisterRequest {
    public final String nombre;
    public final String direccion;
    public final String correoElectronico;
    public final String login;
    public final String contrasena;
    public final String contrasenaConfirmacion;

    public RegisterRequest(String nombre, String direccion, String correoElectronico,
                           String login, String contrasena, String contrasenaConfirmacion) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.correoElectronico = correoElectronico;
        this.login = login;
        this.contrasena = contrasena;
        this.contrasenaConfirmacion = contrasenaConfirmacion;
    }
}
