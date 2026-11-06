package com.example.sabordelhimalaya.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;

import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.CartRepository;
import com.example.sabordelhimalaya.model.dto.ApiError;
import com.example.sabordelhimalaya.model.dto.AuthResponse;
import com.example.sabordelhimalaya.model.dto.RegisterRequest;
import com.example.sabordelhimalaya.util.ApiErrors;
import com.example.sabordelhimalaya.util.Roles;
import com.example.sabordelhimalaya.view.RegistroUsuarioView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Collections;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroUsuario extends BaseActivity {
    private TextInputEditText nombre;
    private TextInputEditText login;
    private TextInputEditText correo;
    private TextInputEditText contrasena;
    private TextInputEditText confirmacion;
    private TextInputEditText direccion;
    private View submit;

    private TextInputLayout nombreContainer;
    private TextInputLayout loginContainer;
    private TextInputLayout correoContainer;
    private TextInputLayout contrasenaContainer;
    private TextInputLayout confirmacionContainer;
    private TextInputLayout direccionContainer;
    private boolean enviando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_registro_usuario);
        new RegistroUsuarioView(findViewById(R.id.registration_content)).applySafeInsets();

        nombre = findViewById(R.id.registration_name);
        login = findViewById(R.id.registration_username);
        correo = findViewById(R.id.registration_email);
        contrasena = findViewById(R.id.registration_password);
        confirmacion = findViewById(R.id.registration_password_confirm);
        direccion = findViewById(R.id.registration_address);
        submit = findViewById(R.id.button_submit_registration);

        nombreContainer = findViewById(R.id.registration_name_container);
        loginContainer = findViewById(R.id.registration_username_container);
        correoContainer = findViewById(R.id.registration_email_container);
        contrasenaContainer = findViewById(R.id.registration_password_container);
        confirmacionContainer = findViewById(R.id.registration_password_confirm_container);
        direccionContainer = findViewById(R.id.registration_address_container);

        findViewById(R.id.button_cancel_registration).setOnClickListener(view -> finish());
        submit.setOnClickListener(view -> registrar());
    }

    private void registrar() {
        if (enviando) {
            return;
        }
        limpiarErrores();
        String vNombre = texto(nombre);
        String vLogin = texto(login);
        String vCorreo = texto(correo);
        String vContrasena = texto(contrasena);
        String vConfirmacion = texto(confirmacion);
        String vDireccion = texto(direccion);

        boolean valido = true;
        if (vNombre.isEmpty()) {
            nombreContainer.setError(getString(R.string.registration_required_fields));
            valido = false;
        } else if (vNombre.length() > 100) {
            nombreContainer.setError(getString(R.string.registration_field_too_long));
            valido = false;
        }
        if (vLogin.isEmpty()) {
            loginContainer.setError(getString(R.string.registration_required_fields));
            valido = false;
        } else if (vLogin.length() > 50) {
            loginContainer.setError(getString(R.string.registration_field_too_long));
            valido = false;
        }
        if (vCorreo.isEmpty()) {
            correoContainer.setError(getString(R.string.registration_required_fields));
            valido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(vCorreo).matches()) {
            correoContainer.setError(getString(R.string.registration_email_invalid));
            valido = false;
        } else if (vCorreo.length() > 100) {
            correoContainer.setError(getString(R.string.registration_field_too_long));
            valido = false;
        }
        if (vContrasena.isEmpty()) {
            contrasenaContainer.setError(getString(R.string.registration_required_fields));
            valido = false;
        } else if (vContrasena.length() > 100) {
            contrasenaContainer.setError(getString(R.string.registration_field_too_long));
            valido = false;
        }
        if (vConfirmacion.isEmpty()) {
            confirmacionContainer.setError(getString(R.string.registration_required_fields));
            valido = false;
        } else if (!vContrasena.equals(vConfirmacion)) {
            confirmacionContainer.setError(getString(R.string.registration_password_mismatch));
            valido = false;
        }
        if (vDireccion.isEmpty()) {
            direccionContainer.setError(getString(R.string.registration_required_fields));
            valido = false;
        } else if (vDireccion.length() > 200) {
            direccionContainer.setError(getString(R.string.registration_field_too_long));
            valido = false;
        }
        if (!valido) {
            return;
        }

        setBusy(true);
        RegisterRequest body = new RegisterRequest(vNombre, vDireccion, vCorreo, vLogin,
                vContrasena, vConfirmacion);
        api().register(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (isFinishing()) {
                    return;
                }
                setBusy(false);
                if (!response.isSuccessful() || response.body() == null) {
                    ApiError error = ApiErrors.parse(response);
                    if (response.code() == 400 || response.code() == 409) {
                        mapearErrores(error != null && error.errores != null
                                ? error.errores
                                : Collections.emptyMap());
                    }
                    if (error != null && error.mensaje != null && !error.mensaje.isEmpty()) {
                        showMessage(error.mensaje);
                    } else {
                        showMessage(ApiErrors.message(response));
                    }
                    return;
                }
                AuthResponse auth = response.body();
                if (auth.rol != null && !Roles.esCliente(auth.rol)) {
                    showMessage(getString(R.string.error_solo_clientes));
                    return;
                }
                // Un registro nuevo no debe heredar la cesta de otra sesión.
                CartRepository.get(RegistroUsuario.this).clear();
                session().saveDireccion(vDireccion);
                Intent intent = new Intent(RegistroUsuario.this, ActivacionCuenta.class);
                intent.putExtra(ActivacionCuenta.EXTRA_CORREO, vCorreo);
                intent.putExtra(ActivacionCuenta.EXTRA_LOGIN, vLogin);
                intent.putExtra(ActivacionCuenta.EXTRA_MENSAJE, auth.mensaje);
                startActivity(intent);
                finish();
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

    private void mapearErrores(Map<String, String> errores) {
        for (Map.Entry<String, String> entry : errores.entrySet()) {
            String campo = entry.getKey();
            String mensaje = entry.getValue();
            if ("nombre".equals(campo)) {
                nombreContainer.setError(mensaje);
            } else if ("login".equals(campo)) {
                loginContainer.setError(mensaje);
            } else if ("correoElectronico".equals(campo)) {
                correoContainer.setError(mensaje);
            } else if ("contrasena".equals(campo)) {
                contrasenaContainer.setError(mensaje);
            } else if ("contrasenaConfirmacion".equals(campo)) {
                confirmacionContainer.setError(mensaje);
            } else if ("direccion".equals(campo)) {
                direccionContainer.setError(mensaje);
            }
        }
    }

    private void limpiarErrores() {
        nombreContainer.setError(null);
        loginContainer.setError(null);
        correoContainer.setError(null);
        contrasenaContainer.setError(null);
        confirmacionContainer.setError(null);
        direccionContainer.setError(null);
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
