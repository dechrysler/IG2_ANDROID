package com.example.sabordelhimalaya.controller;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.view.CestaView;

public class Cesta extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_cesta);
        CestaView cestaView = new CestaView(findViewById(R.id.basket_content));
        mostrarCesta(cestaView);
        cestaView.initialize(() -> {
            Intent intent = new Intent(this, Principal2.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }, () -> {
            startActivity(new Intent(this, Pedidos.class));
            finish();
        });
    }

    private void mostrarCesta(CestaView vista) {
        vista.mostrarProductos(Principal2.getCesta().getCantidadesPlatos(),
                Principal2.getCesta().getCantidadesMenus(), (plato, cantidad) -> {
            Principal2.getCesta().cambiarCantidad(plato, cantidad);
            mostrarCesta(vista);
        }, (menu, cantidad) -> {
            Principal2.getCesta().cambiarCantidad(menu, cantidad);
            mostrarCesta(vista);
        });
    }
}
