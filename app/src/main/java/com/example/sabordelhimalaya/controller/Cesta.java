package com.example.sabordelhimalaya.controller;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.HacerPedido;
import com.example.sabordelhimalaya.model.PedidoLocalRepository;
import com.example.sabordelhimalaya.view.CestaView;
import com.google.android.material.snackbar.Snackbar;
import java.io.IOException;
import org.json.JSONException;

public class Cesta extends AppCompatActivity {
    private static final int ID_USUARIO_PRUEBA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_cesta);
        CestaView cestaView = new CestaView(findViewById(R.id.basket_content));
        mostrarCesta(cestaView);
        findViewById(R.id.button_place_order).setOnClickListener(view -> guardarPedido(cestaView));
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

    private void guardarPedido(CestaView vista) {
        com.example.sabordelhimalaya.model.Cesta cestaActual = Principal2.getCesta();
        if (cestaActual.getNumeroElementos() == 0) return;
        try {
            HacerPedido pedido = new HacerPedido(ID_USUARIO_PRUEBA, cestaActual);
            String json = new PedidoLocalRepository().guardar(getFilesDir(), pedido);
            if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                for (String linea : json.split("\n")) {
                    Log.d("PedidoGuardado", linea);
                }
            }
        } catch (IOException | JSONException exception) {
            Snackbar.make(findViewById(R.id.basket_content), R.string.basket_save_error,
                    Snackbar.LENGTH_LONG).show();
            return;
        }
        cestaActual.vaciar();
        mostrarCesta(vista);
        Snackbar.make(findViewById(R.id.basket_content), R.string.basket_saved,
                Snackbar.LENGTH_SHORT).show();
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
