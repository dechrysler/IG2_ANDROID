package com.example.sabordelhimalaya.controller;

import android.os.Bundle;
import android.content.Intent;
import android.util.Patterns;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.view.RegistroUsuarioView;

public class RegistroUsuario extends AppCompatActivity {
    private String[] datosUsuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_registro_usuario);
        RegistroUsuarioView registroView = new RegistroUsuarioView(findViewById(R.id.registration_content));
        registroView.applySafeInsets();
        findViewById(R.id.button_cancel_registration).setOnClickListener(view -> finish());
        findViewById(R.id.button_submit_registration).setOnClickListener(view -> {
            datosUsuario = registroView.recogerDatosUsuario();
            boolean[] camposFaltantes = new boolean[datosUsuario.length];
            boolean faltanDatos = false;
            for (int i = 0; i < datosUsuario.length; i++) {
                camposFaltantes[i] = datosUsuario[i].trim().isEmpty();
                faltanDatos = faltanDatos || camposFaltantes[i];
            }
            registroView.mostrarCamposFaltantes(camposFaltantes);
            if (faltanDatos) {
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(datosUsuario[2]).matches()) {
                registroView.mostrarErrorCorreo();
                return;
            }
            startActivity(new Intent(this, ActivacionCuenta.class));
        });
    }
}
