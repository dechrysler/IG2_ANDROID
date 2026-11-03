package com.example.sabordelhimalaya.controller;

import android.os.Bundle;
import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.view.ActivacionCuentaView;
import com.google.android.material.snackbar.Snackbar;

public class ActivacionCuenta extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_activacion_cuenta);
        new ActivacionCuentaView(findViewById(R.id.activation_content)).initialize();
        findViewById(R.id.button_send_activation).setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.button_resend_activation).setOnClickListener(view -> showPendingService());
    }

    private void showPendingService() {
        Snackbar.make(findViewById(R.id.activation_content), R.string.activation_pending,
                Snackbar.LENGTH_LONG).show();
    }
}
