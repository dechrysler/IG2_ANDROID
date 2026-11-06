package com.example.sabordelhimalaya.controller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AlertDialog;

import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.CartRepository;
import com.example.sabordelhimalaya.model.Pedido;
import com.example.sabordelhimalaya.model.dto.OrderResponse;
import com.example.sabordelhimalaya.util.ApiErrors;
import com.example.sabordelhimalaya.view.DetallePedidoView;

import java.time.LocalDateTime;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetallePedido extends BaseActivity {
    public static final String EXTRA_PEDIDO_ID = "extra_pedido_id";
    private static final long INTERVALO_POLLING_MS = 20_000L;

    private DetallePedidoView view;
    private String pedidoId;
    private Pedido actual;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean cargando;

    private final Runnable polling = new Runnable() {
        @Override
        public void run() {
            cargar(false);
            handler.postDelayed(this, INTERVALO_POLLING_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_detalle_pedido);
        if (!ensureCliente()) {
            return;
        }
        pedidoId = getIntent().getStringExtra(EXTRA_PEDIDO_ID);
        if (pedidoId == null || pedidoId.isEmpty()) {
            showMessage(getString(R.string.detail_not_found));
            finish();
            return;
        }
        view = new DetallePedidoView(findViewById(R.id.detail_content));
        view.initialize(this::finish, this::modificar, this::confirmarCancelacion);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ensureCliente() || view == null) {
            return;
        }
        cargar(actual == null);
        handler.removeCallbacks(polling);
        handler.postDelayed(polling, INTERVALO_POLLING_MS);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(polling);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(polling);
        super.onDestroy();
    }

    private void cargar(boolean mostrarCarga) {
        if (cargando) {
            return;
        }
        cargando = true;
        if (mostrarCarga) {
            view.showLoading(false);
        }
        api().order(pedidoId).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                cargando = false;
                if (isFinishing()) {
                    return;
                }
                if (handle401(response)) {
                    return;
                }
                if (response.code() == 404) {
                    showMessage(getString(R.string.detail_not_found));
                    irAPedidos();
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    if (actual == null) {
                        view.showError(ApiErrors.message(response), () -> cargar(true), false);
                    } else {
                        showMessage(ApiErrors.message(response));
                    }
                    return;
                }
                actual = Pedido.from(response.body());
                view.render(actual);
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                cargando = false;
                if (isFinishing()) {
                    return;
                }
                if (actual == null) {
                    view.showError(ApiErrors.networkMessage(t), () -> cargar(true), false);
                } else {
                    showMessage(ApiErrors.networkMessage(t));
                }
            }
        });
    }

    private void modificar() {
        if (actual == null || !actual.isModificable()) {
            showMessage(getString(R.string.detail_error));
            return;
        }
        CartRepository.get(this).loadForEdit(actual);
        startActivity(new Intent(this, Cesta.class));
    }

    private void confirmarCancelacion() {
        if (actual == null || !actual.isCancelable()) {
            showMessage(getString(R.string.detail_error));
            return;
        }
        boolean pasadoPlazo = actual.getFechaHora() != null
                && actual.getFechaHora().plusMinutes(10).isBefore(LocalDateTime.now());
        String mensaje = getString(R.string.detail_cancel_confirm);
        if (pasadoPlazo) {
            mensaje += "\n\n" + getString(R.string.detail_cancel_charge_warning);
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.detail_cancel_confirm_title)
                .setMessage(mensaje)
                .setPositiveButton(R.string.detail_cancel_ok, (dialog, which) -> cancelar())
                .setNegativeButton(R.string.action_back, null)
                .show();
    }

    private void cancelar() {
        view.setBusy(true);
        api().cancel(pedidoId, null).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (isFinishing()) {
                    return;
                }
                view.setBusy(false);
                if (handle401(response)) {
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    showMessage(ApiErrors.message(response));
                    return;
                }
                actual = Pedido.from(response.body());
                view.render(actual);
                showMessage(getString(R.string.detail_cancelled));
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                if (isFinishing()) {
                    return;
                }
                view.setBusy(false);
                showMessage(ApiErrors.networkMessage(t));
            }
        });
    }

    private void irAPedidos() {
        Intent intent = new Intent(this, Pedidos.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
