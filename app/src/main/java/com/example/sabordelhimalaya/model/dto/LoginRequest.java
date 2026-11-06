package com.example.sabordelhimalaya.model.dto;

public class LoginRequest {
    public final String login;
    public final String contrasena;

    public LoginRequest(String login, String contrasena) {
        this.login = login;
        this.contrasena = contrasena;
    }
}
