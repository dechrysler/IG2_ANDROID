package com.example.sabordelhimalaya.controller;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.CartRepository;
import com.example.sabordelhimalaya.model.remote.ApiClient;
import com.example.sabordelhimalaya.model.remote.ApiService;
import com.example.sabordelhimalaya.session.SessionManager;
import com.example.sabordelhimalaya.util.ApiErrors;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Response;

public abstract class BaseActivity extends AppCompatActivity {

    protected ApiService api() {
        return ApiClient.api();
    }

    protected SessionManager session() {
        return SessionManager.get();
    }

    protected void showMessage(String mensaje) {
        if (mensaje == null || mensaje.isEmpty()) {
            return;
        }
        Snackbar.make(findViewById(android.R.id.content), mensaje, Snackbar.LENGTH_LONG).show();
    }

    protected String errorMessage(Response<?> response) {
        return ApiErrors.message(response);
    }

    protected boolean handle401(Response<?> response) {
        if (response != null && response.code() == 401) {
            session().clear();
            goToLogin();
            return true;
        }
        return false;
    }

    protected boolean ensureCliente() {
        if (!session().isLoggedIn() || !session().isCliente()) {
            session().clear();
            goToLogin();
            return false;
        }
        return true;
    }

    protected void goToLogin() {

        CartRepository.get(this).clear();
        Intent intent = new Intent(this, InicioSesion.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    protected void goToCarta() {
        Intent intent = new Intent(this, Principal2.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    protected void mostrarSoloClientes() {
        showMessage(getString(R.string.error_solo_clientes));
    }
}
