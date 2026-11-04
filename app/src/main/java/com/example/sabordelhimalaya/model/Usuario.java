package com.example.sabordelhimalaya.model;

public class Usuario {
    private final String nombre;
    private final String nombreUsuario;
    private final String correo;
    private final String contrasena;
    private final String direccion;

    public Usuario(String nombre, String nombreUsuario, String correo,
                   String contrasena, String direccion) {
        this.nombre = nombre;
        this.nombreUsuario = nombreUsuario;
        this.correo = correo;
        this.contrasena = contrasena;
        this.direccion = direccion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getCorreo() {
        return correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public String getDireccion() {
        return direccion;
    }
}
