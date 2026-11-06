package com.example.sabordelhimalaya.controller;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AlertDialog;

import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.CartRepository;

public class Perfil extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_perfil);
        if (!ensureCliente()) {
            return;
        }

        findViewById(R.id.button_profile_back).setOnClickListener(view -> finish());
        findViewById(R.id.button_profile_continue).setOnClickListener(view -> finish());
        findViewById(R.id.button_profile_logout).setOnClickListener(view -> confirmarLogout());

        String nombre = session().getNombre() != null ? session().getNombre() : "";
        TextView avatar = findViewById(R.id.profile_avatar);
        avatar.setText(nombre.isEmpty() ? "?" : nombre.substring(0, 1).toUpperCase());
        ((TextView) findViewById(R.id.profile_name)).setText(nombre);
        ((TextView) findViewById(R.id.profile_login)).setText(valor(session().getLogin()));
        ((TextView) findViewById(R.id.profile_email)).setText(valor(session().getCorreo()));
        ((TextView) findViewById(R.id.profile_address)).setText(
                session().getDireccion() == null || session().getDireccion().isEmpty()
                        ? getString(R.string.profile_address_empty)
                        : session().getDireccion());
    }

    private String valor(String texto) {
        return texto == null || texto.isEmpty() ? "—" : texto;
    }

    private void confirmarLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.profile_logout)
                .setMessage(R.string.profile_logout_confirm)
                .setPositiveButton(R.string.profile_logout, (dialog, which) -> {
                    CartRepository.get(this).clear();
                    session().clear();
                    goToLogin();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
