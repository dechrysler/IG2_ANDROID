package com.example.sabordelhimalaya.controller;

import android.os.Bundle;
import android.content.Intent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.view.InicioSesionView;

public class InicioSesion extends AppCompatActivity {
    private String[] Credenciades;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_inicio_sesion);
        InicioSesionView inicioView = new InicioSesionView(findViewById(R.id.login_content));
        inicioView.initialize();
        findViewById(R.id.button_cancel_login).setOnClickListener(view -> finish());
        findViewById(R.id.button_submit_login).setOnClickListener(view -> {
            Credenciades = inicioView.recogerCredenciades();
            boolean usuarioVacio = Credenciades[0].trim().isEmpty();
            boolean contrasenaVacia = Credenciades[1].trim().isEmpty();
            inicioView.mostrarErroresCredenciades(usuarioVacio, contrasenaVacia);
            if (usuarioVacio || contrasenaVacia) {
                return;
            }
            try (BufferedReader lector = new BufferedReader(new InputStreamReader(
                    getAssets().open("datos.txt"), StandardCharsets.UTF_8))) {
                String usuarioGuardado = lector.readLine();
                String contrasenaGuardada = lector.readLine();
                if (usuarioGuardado == null || contrasenaGuardada == null) {
                    throw new IOException("Archivo de credenciales incompleto");
                }
                boolean usuarioIncorrecto = !usuarioGuardado.equals(Credenciades[0]);
                boolean contrasenaIncorrecta = !contrasenaGuardada.equals(Credenciades[1]);
                inicioView.mostrarErroresCredenciades(usuarioIncorrecto, contrasenaIncorrecta);
                if (usuarioIncorrecto || contrasenaIncorrecta) {
                    return;
                }
            } catch (IOException exception) {
                inicioView.mostrarErrorLectura();
                return;
            }
            startActivity(new Intent(this, Principal2.class));
        });
    }
}
