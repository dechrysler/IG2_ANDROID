package com.example.sabordelhimalaya.controller;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.dto.UserProfile;
import com.example.sabordelhimalaya.util.ApiErrors;
import com.example.sabordelhimalaya.util.Roles;
import com.example.sabordelhimalaya.view.WelcomeView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Controlador de entrada: valida la sesión guardada antes de mostrar la bienvenida. */
public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_RED_LOCAL = 1001;
    private static final String PERMISO_RED_LOCAL = "android.permission.ACCESS_LOCAL_NETWORK";

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

        if (!tienePermisoRedLocal()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{PERMISO_RED_LOCAL}, REQUEST_RED_LOCAL);
        } else if (com.example.sabordelhimalaya.session.SessionManager.get().isLoggedIn()) {
            validarSesion();
        }
    }

    private boolean tienePermisoRedLocal() {
        if (Build.VERSION.SDK_INT < 36) {
            return true;
        }
        return ContextCompat.checkSelfPermission(this, PERMISO_RED_LOCAL)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_RED_LOCAL) {
            return;
        }
        if (tienePermisoRedLocal()) {
            if (com.example.sabordelhimalaya.session.SessionManager.get().isLoggedIn()) {
                validarSesion();
            }
        } else {
            mostrarMensaje(getString(R.string.error_local_network));
        }
    }

    private void validarSesion() {
        View login = findViewById(R.id.button_login);
        View register = findViewById(R.id.button_register);
        login.setEnabled(false);
        register.setEnabled(false);
        com.example.sabordelhimalaya.model.remote.ApiClient.api().me()
                .enqueue(new Callback<UserProfile>() {
                    @Override
                    public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                        if (isFinishing()) {
                            return;
                        }
                        if (response.code() == 401) {
                            com.example.sabordelhimalaya.session.SessionManager.get().clear();
                            habilitarBotones(login, register);
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null) {
                            habilitarBotones(login, register);
                            return;
                        }
                        UserProfile profile = response.body();
                        if (!Roles.esCliente(profile.rol)) {
                            com.example.sabordelhimalaya.session.SessionManager.get().clear();
                            habilitarBotones(login, register);
                            mostrarMensaje(getString(R.string.error_solo_clientes));
                            return;
                        }
                        com.example.sabordelhimalaya.session.SessionManager.get().updateProfile(
                                profile.usuarioId, profile.nombre,
                                profile.correoElectronico, profile.rol);
                        startActivity(new Intent(MainActivity.this, Principal2.class));
                        finish();
                    }

                    @Override
                    public void onFailure(Call<UserProfile> call, Throwable t) {
                        if (isFinishing()) {
                            return;
                        }
                        habilitarBotones(login, register);
                        mostrarMensaje(ApiErrors.networkMessage(t));
                    }
                });
    }

    private void habilitarBotones(View login, View register) {
        login.setEnabled(true);
        register.setEnabled(true);
    }

    private void mostrarMensaje(String mensaje) {
        com.google.android.material.snackbar.Snackbar.make(
                findViewById(android.R.id.content), mensaje,
                com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show();
    }
}
