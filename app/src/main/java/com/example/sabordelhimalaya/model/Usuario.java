package com.example.sabordelhimalaya.model;

public class Usuario {
    private final String usuarioId;
    private final String nombre;
    private final String login;
    private final String correoElectronico;
    private final String rol;
    private final String direccion;

    public Usuario(String usuarioId, String nombre, String login,
                   String correoElectronico, String rol, String direccion) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.login = login;
        this.correoElectronico = correoElectronico;
        this.rol = rol;
        this.direccion = direccion;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getLogin() {
        return login;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public String getRol() {
        return rol;
    }

    public String getDireccion() {
        return direccion;
    }

    public boolean esCliente() {
        return "CLIENTE".equals(rol);
    }
}
