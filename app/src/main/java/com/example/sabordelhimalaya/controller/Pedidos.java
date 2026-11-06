package com.example.sabordelhimalaya.controller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;

import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.Pedido;
import com.example.sabordelhimalaya.model.dto.OrderResponse;
import com.example.sabordelhimalaya.util.ApiErrors;
import com.example.sabordelhimalaya.view.PedidosView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Pedidos extends BaseActivity implements PedidosView.Listener {
    private static final long INTERVALO_POLLING_MS = 25_000L;

    private PedidosView view;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean cargando;
    private boolean hayDatos;

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
        setContentView(R.layout.activity_pedidos);
        if (!ensureCliente()) {
            return;
        }
        view = new PedidosView(findViewById(R.id.orders_content), this);
        view.initialize(() -> {
            Intent intent = new Intent(this, Principal2.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }, () -> {
            Intent intent = new Intent(this, Cesta.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ensureCliente() || view == null) {
            return;
        }
        cargar(true);
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
        if (mostrarCarga && !hayDatos) {
            view.showLoading();
        }
        api().myOrders().enqueue(new Callback<List<OrderResponse>>() {
            @Override
            public void onResponse(Call<List<OrderResponse>> call,
                                   Response<List<OrderResponse>> response) {
                cargando = false;
                if (isFinishing()) {
                    return;
                }
                if (handle401(response)) {
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    if (!hayDatos) {
                        view.showError(ApiErrors.message(response), () -> cargar(true));
                    } else {
                        showMessage(ApiErrors.message(response));
                    }
                    return;
                }
                hayDatos = true;
                List<Pedido> pedidos = new ArrayList<>();
                for (OrderResponse order : response.body()) {
                    pedidos.add(Pedido.from(order));
                }
                if (pedidos.isEmpty()) {
                    view.showEmpty();
                } else {
                    view.showOrders(pedidos);
                }
            }

            @Override
            public void onFailure(Call<List<OrderResponse>> call, Throwable t) {
                cargando = false;
                if (isFinishing()) {
                    return;
                }
                if (!hayDatos) {
                    view.showError(ApiErrors.networkMessage(t), () -> cargar(true));
                } else {
                    showMessage(ApiErrors.networkMessage(t));
                }
            }
        });
    }

    @Override
    public void onOrderClick(Pedido pedido) {
        Intent intent = new Intent(this, DetallePedido.class);
        intent.putExtra(DetallePedido.EXTRA_PEDIDO_ID, pedido.getId());
        startActivity(intent);
    }
}
