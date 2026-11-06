package com.example.sabordelhimalaya.controller;

import android.os.Bundle;
import android.content.Intent;
import java.io.IOException;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.view.Principal2View;
import com.example.sabordelhimalaya.model.PlatoRepository;
import com.example.sabordelhimalaya.model.MenuDiarioRepository;
import com.example.sabordelhimalaya.model.Plato;
import java.util.List;

public class Principal2 extends AppCompatActivity {
    private Principal2View principalView;
    private static com.example.sabordelhimalaya.model.Cesta cesta =
            new com.example.sabordelhimalaya.model.Cesta();

    static com.example.sabordelhimalaya.model.Cesta getCesta() {
        return cesta;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            cesta = new com.example.sabordelhimalaya.model.Cesta();
        }
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_principal2);
        principalView = new Principal2View(findViewById(R.id.principal_content));
        principalView.initialize(
                () -> startActivity(new Intent(this, Cesta.class)),
                () -> startActivity(new Intent(this, Pedidos.class)));
        try {
            List<Plato> carta = new PlatoRepository().cargar(getAssets().open("platos.txt"));
            principalView.mostrarPlatos(carta,
                    (plato, cantidad) -> {
                        for (int unidad = 0; unidad < cantidad; unidad++) {
                            cesta.anadirPlato(plato);
                        }
                        principalView.mostrarContadorCesta(cesta.getNumeroElementos());
                    });
            try {
                principalView.configurarMenuDiario(new MenuDiarioRepository()
                        .cargar(getAssets().open("menu_diario.txt"), carta), menu -> {
                    cesta.anadirMenuDiario(menu);
                    principalView.mostrarContadorCesta(cesta.getNumeroElementos());
                });
            } catch (IOException exception) {
                principalView.configurarMenuDiario(null, cesta::anadirMenuDiario);
            }
        } catch (IOException exception) {
            principalView.mostrarErrorCarga();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        principalView.mostrarContadorCesta(cesta.getNumeroElementos());
    }

}
