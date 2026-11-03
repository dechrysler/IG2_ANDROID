package com.example.sabordelhimalaya.controller;

import android.os.Bundle;
import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.view.RegistroUsuarioView;

public class RegistroUsuario extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_registro_usuario);
        new RegistroUsuarioView(findViewById(R.id.registration_content)).applySafeInsets();
        findViewById(R.id.button_cancel_registration).setOnClickListener(view -> finish());
        findViewById(R.id.button_submit_registration).setOnClickListener(view ->
                startActivity(new Intent(this, ActivacionCuenta.class)));
    }
}
