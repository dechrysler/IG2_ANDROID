package com.example.sabordelhimalaya.controller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;

import com.example.sabordelhimalaya.BuildConfig;
import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.dto.AuthResponse;
import com.example.sabordelhimalaya.model.dto.ActivateRequest;
import com.example.sabordelhimalaya.model.dto.ResendCodeRequest;
import com.example.sabordelhimalaya.util.ApiErrors;
import com.example.sabordelhimalaya.util.Roles;
import com.example.sabordelhimalaya.view.ActivacionCuentaView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivacionCuenta extends BaseActivity {
    public static final String EXTRA_CORREO = "extra_correo";
    public static final String EXTRA_LOGIN = "extra_login";
    public static final String EXTRA_MENSAJE = "extra_mensaje";

    private static final long COOLDOWN_MS = 5 * 60 * 1000L;

    private TextInputEditText codigo;
    private TextInputLayout codigoContainer;
    private View enviar;
    private View reenviar;
    private String correo;
    private String login;
    private boolean enviando;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long cooldownFin;
    private final Runnable cooldownTick = new Runnable() {
        @Override
        public void run() {
            long restante = cooldownFin - System.currentTimeMillis();
            if (restante <= 0) {
                reenviar.setEnabled(true);
                reenviar.setAlpha(1f);
                ((com.google.android.material.button.MaterialButton) reenviar)
                        .setText(R.string.activation_resend);
                return;
            }
            long segundos = (restante + 999) / 1000;
            reenviar.setEnabled(false);
            reenviar.setAlpha(0.6f);
            ((com.google.android.material.button.MaterialButton) reenviar).setText(
                    String.format(java.util.Locale.getDefault(),
                            "Reenviar en %d:%02d", segundos / 60, segundos % 60));
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_activacion_cuenta);
        new ActivacionCuentaView(findViewById(R.id.activation_content)).initialize();

        correo = getIntent().getStringExtra(EXTRA_CORREO);
        login = getIntent().getStringExtra(EXTRA_LOGIN);
        String mensaje = getIntent().getStringExtra(EXTRA_MENSAJE);

        if (correo == null || correo.isEmpty()) {
            showMessage(getString(R.string.activation_email_missing));
            finish();
            return;
        }
        ((android.widget.TextView) findViewById(R.id.activation_email)).setText(correo);
        findViewById(R.id.activation_dev_hint)
                .setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);

        codigo = findViewById(R.id.activation_code);
        codigoContainer = findViewById(R.id.activation_code_container);
        enviar = findViewById(R.id.button_send_activation);
        reenviar = findViewById(R.id.button_resend_activation);

        enviar.setOnClickListener(view -> activar());
        reenviar.setOnClickListener(view -> reenviarCodigo());

        if (mensaje != null && !mensaje.isEmpty()) {
            showMessage(mensaje);
        }
    }

    private void activar() {
        if (enviando) {
            return;
        }
        String valor = codigo.getText() == null ? "" : codigo.getText().toString().trim();
        codigoContainer.setError(null);
        if (!valor.matches("\\d{6}")) {
            codigoContainer.setError(getString(R.string.activation_code_invalid));
            return;
        }
        setBusy(true);
        api().activate(new ActivateRequest(correo, valor)).enqueue(new Callback<AuthResponse>() {
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
                    session().clear();
                    mostrarSoloClientes();
                    return;
                }
                if (auth.token == null || auth.token.isEmpty()) {
                    showMessage(getString(R.string.error_generic));
                    return;
                }
                session().saveToken(auth.token);
                session().saveProfile(auth.usuarioId, auth.nombre, login,
                        auth.correoElectronico, auth.rol, session().getDireccion());
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

    private void reenviarCodigo() {
        reenviar.setEnabled(false);
        reenviar.setAlpha(0.6f);
        api().resendCode(new ResendCodeRequest(correo)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (isFinishing()) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    iniciarCooldown();
                    showMessage(response.body().mensaje != null
                            ? response.body().mensaje
                            : "Código reenviado.");
                } else {
                    reenviar.setEnabled(true);
                    reenviar.setAlpha(1f);
                    showMessage(ApiErrors.message(response));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                if (isFinishing()) {
                    return;
                }
                reenviar.setEnabled(true);
                reenviar.setAlpha(1f);
                showMessage(ApiErrors.networkMessage(t));
            }
        });
    }

    private void iniciarCooldown() {
        cooldownFin = System.currentTimeMillis() + COOLDOWN_MS;
        handler.removeCallbacks(cooldownTick);
        handler.post(cooldownTick);
    }

    private void setBusy(boolean busy) {
        enviando = busy;
        enviar.setEnabled(!busy);
        enviar.setAlpha(busy ? 0.6f : 1f);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(cooldownTick);
        super.onDestroy();
    }
}
