package com.example.sabordelhimalaya.model.dto;

public class ActivateRequest {
    public final String correoElectronico;
    public final String codigo;

    public ActivateRequest(String correoElectronico, String codigo) {
        this.correoElectronico = correoElectronico;
        this.codigo = codigo;
    }
}
