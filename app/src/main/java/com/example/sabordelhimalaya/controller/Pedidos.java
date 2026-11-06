package com.example.sabordelhimalaya.controller;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.view.PedidosView;
import com.example.sabordelhimalaya.model.PedidoLocalRepository;
import com.google.android.material.snackbar.Snackbar;
import java.io.IOException;
import java.time.DateTimeException;
import org.json.JSONException;

public class Pedidos extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_pedidos);
        PedidosView vista = new PedidosView(findViewById(R.id.orders_content));
        vista.initialize(() -> {
            Intent intent = new Intent(this, Principal2.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }, () -> {
            startActivity(new Intent(this, Cesta.class));
            finish();
        });
        try {
            vista.mostrarPedidos(new PedidoLocalRepository().cargar(getFilesDir()));
        } catch (IOException | JSONException | DateTimeException | IllegalArgumentException exception) {
            Snackbar.make(findViewById(R.id.orders_content), R.string.orders_read_error,
                    Snackbar.LENGTH_LONG).show();
        }
    }
}
