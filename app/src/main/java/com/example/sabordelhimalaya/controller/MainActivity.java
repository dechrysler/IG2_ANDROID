package com.example.sabordelhimalaya.controller;

import android.os.Bundle;
import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.view.WelcomeView;

/** Controlador de entrada de la pantalla de bienvenida. */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_main);
        new WelcomeView(findViewById(R.id.welcome_content)).applySafeInsets();
        findViewById(R.id.button_login).setOnClickListener(view ->
                startActivity(new Intent(this, InicioSesion.class)));
        findViewById(R.id.button_register).setOnClickListener(view ->
                startActivity(new Intent(this, RegistroUsuario.class)));
    }
}
