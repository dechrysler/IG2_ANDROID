package com.example.sabordelhimalaya.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;

import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.CartRepository;
import com.example.sabordelhimalaya.model.dto.AuthResponse;
import com.example.sabordelhimalaya.model.dto.LoginRequest;
import com.example.sabordelhimalaya.session.SessionManager;
import com.example.sabordelhimalaya.util.ApiErrors;
import com.example.sabordelhimalaya.util.Roles;
import com.example.sabordelhimalaya.view.InicioSesionView;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InicioSesion extends BaseActivity {
    private TextInputEditText username;
    private TextInputEditText password;
    private View submit;
    private boolean enviando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_inicio_sesion);
        new InicioSesionView(findViewById(R.id.login_content)).initialize();

        username = findViewById(R.id.login_username);
        password = findViewById(R.id.login_password);
        submit = findViewById(R.id.button_submit_login);

        findViewById(R.id.button_cancel_login).setOnClickListener(view -> finish());
        findViewById(R.id.button_go_register).setOnClickListener(view ->
                startActivity(new Intent(this, RegistroUsuario.class)));
        submit.setOnClickListener(view -> iniciarSesion());
        password.setOnEditorActionListener((view, actionId, event) -> {
            iniciarSesion();
            return true;
        });
    }

    private void iniciarSesion() {
        if (enviando) {
            return;
        }
        String login = texto(username);
        String clave = texto(password);
        if (login.isEmpty() || clave.isEmpty()) {
            showMessage(getString(R.string.login_required_fields));
            return;
        }
        setBusy(true);
        api().login(new LoginRequest(login, clave)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (isFinishing()) {
                    return;
                }
                setBusy(false);
                if (!response.isSuccessful() || response.body() == null) {
                    showMessage(ApiErrors.message(response));
                    return;
                }
                AuthResponse auth = response.body();

                if (!Roles.esCliente(auth.rol)) {
                    CartRepository.get(InicioSesion.this).clear();
                    session().clear();
                    mostrarSoloClientes();
                    return;
                }
                if (auth.token == null || auth.token.isEmpty()) {
                    showMessage(getString(R.string.error_generic));
                    return;
                }

                String usuarioAnterior = session().getUsuarioId();
                if (usuarioAnterior == null || !usuarioAnterior.equals(auth.usuarioId)) {
                    CartRepository.get(InicioSesion.this).clear();

                    session().clear();
                }
                SessionManager.get().saveToken(auth.token);
                String direccion = auth.direccion != null && !auth.direccion.isEmpty()
                        ? auth.direccion
                        : session().getDireccion();
                SessionManager.get().saveProfile(auth.usuarioId, auth.nombre, login,
                        auth.correoElectronico, auth.rol, direccion);
                goToCarta();
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (isFinishing()) {
                    return;
                }
                setBusy(false);
                showMessage(ApiErrors.networkMessage(t));
            }
        });
    }

    private void setBusy(boolean busy) {
        enviando = busy;
        submit.setEnabled(!busy);
        submit.setAlpha(busy ? 0.6f : 1f);
    }

    private String texto(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
