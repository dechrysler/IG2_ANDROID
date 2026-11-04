package com.example.sabordelhimalaya.controller;

import android.os.Bundle;
import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.view.InicioSesionView;

public class InicioSesion extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_inicio_sesion);
        new InicioSesionView(findViewById(R.id.login_content)).initialize();
        findViewById(R.id.button_cancel_login).setOnClickListener(view -> finish());
        findViewById(R.id.button_submit_login).setOnClickListener(view ->
                startActivity(new Intent(this, Principal2.class)));
    }
}
