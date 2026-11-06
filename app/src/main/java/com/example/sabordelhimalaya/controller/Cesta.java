package com.example.sabordelhimalaya.controller;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;

import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.CartRepository;
import com.example.sabordelhimalaya.model.dto.CreateOrderRequest;
import com.example.sabordelhimalaya.model.dto.OrderPreviewResponse;
import com.example.sabordelhimalaya.model.dto.OrderResponse;
import com.example.sabordelhimalaya.util.ApiErrors;
import com.example.sabordelhimalaya.view.CestaView;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Cesta extends BaseActivity implements CestaView.Listener {
    private CestaView view;
    private CartRepository cart;
    private boolean enviando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_cesta);
        if (!ensureCliente()) {
            return;
        }
        cart = CartRepository.get(this);
        view = new CestaView(findViewById(R.id.basket_content), this);
        view.initialize(() -> {
            Intent intent = new Intent(this, Principal2.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }, () -> {
            Intent intent = new Intent(this, Pedidos.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        String direccion = cart.getEditingAddress();
        if (direccion == null || direccion.isEmpty()) {
            direccion = session().getDireccion();
        }
        view.setDireccion(direccion);
        view.refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ensureCliente() || view == null) {
            return;
        }
        view.refresh();
    }

    @Override
    public void onReview() {
        String direccion = view.getDireccion();
        if (direccion.isEmpty()) {
            view.showDireccionError(getString(R.string.basket_address_required));
            return;
        }
        view.showDireccionError(null);
        view.setBusy(true);
        api().preview(cart.toRequest(direccion)).enqueue(new Callback<OrderPreviewResponse>() {
            @Override
            public void onResponse(Call<OrderPreviewResponse> call,
                                   Response<OrderPreviewResponse> response) {
                if (isFinishing()) {
                    return;
                }
                view.setBusy(false);
                if (handle401(response)) {
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    view.clearPreview();
                    showMessage(ApiErrors.message(response));
                    return;
                }
                view.showPreview(response.body());
            }

            @Override
            public void onFailure(Call<OrderPreviewResponse> call, Throwable t) {
                if (isFinishing()) {
                    return;
                }
                view.setBusy(false);
                view.clearPreview();
                showMessage(ApiErrors.networkMessage(t));
            }
        });
    }

    @Override
    public void onConfirm() {
        if (enviando) {
            return;
        }
        if (!view.isPreviewVisible()) {
            showMessage(getString(R.string.basket_preview_required));
            return;
        }
        String direccion = view.getDireccion();
        if (direccion.isEmpty()) {
            view.showDireccionError(getString(R.string.basket_address_required));
            return;
        }

        view.setBusy(true);
        enviando = true;
        CreateOrderRequest body = cart.toRequest(direccion);
        String editingId = cart.getEditingOrderId();
        Callback<OrderResponse> callback = new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (isFinishing()) {
                    return;
                }
                if (handle401(response)) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    OrderResponse pedido = response.body();
                    cart.clear();
                    view.refresh();
                    String mensaje = editingId != null
                            ? getString(R.string.basket_updated)
                            : getString(R.string.basket_created, pedido.numeroPedido);
                    showMessage(mensaje);
                    Intent intent = new Intent(Cesta.this, DetallePedido.class);
                    intent.putExtra(DetallePedido.EXTRA_PEDIDO_ID, pedido.id);
                    startActivity(intent);
                    finish();
                    return;
                }
                // Nunca se reintenta automáticamente: se evita duplicar pedidos.
                enviando = false;
                view.setBusy(false);
                String mensaje = ApiErrors.message(response);
                if (esDisponibilidad(mensaje)) {
                    view.clearPreview();
                    showMessage(getString(R.string.basket_availability));
                } else {
                    showMessage(mensaje);
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                if (isFinishing()) {
                    return;
                }
                enviando = false;
                view.setBusy(false);
                showMessage(ApiErrors.networkMessage(t));
            }
        };

        if (editingId != null) {
            api().update(editingId, body).enqueue(callback);
        } else {
            api().create(body).enqueue(callback);
        }
    }

    @Override
    public void onCartChanged() {
        if (view != null) {
            view.clearPreview();
        }
    }

    @Override
    protected void onDestroy() {
        if (isFinishing() && cart != null && cart.getEditingOrderId() != null) {
            // Si se abandona la modificación, no dejar la cesta en modo edición.
            cart.clear();
        }
        super.onDestroy();
    }

    private boolean esDisponibilidad(String mensaje) {
        if (mensaje == null) {
            return false;
        }
        String texto = mensaje.toLowerCase(Locale.forLanguageTag("es-ES"));
        return texto.contains("no está disponible")
                || texto.contains("no esta disponible")
                || texto.contains("no está configurado")
                || texto.contains("no esta configurado");
    }

    private void mostrarCesta(CestaView vista) {
        vista.mostrarProductos(Principal2.getCesta().getCantidadesPlatos(),
                Principal2.getCesta().getCantidadesMenus(), (plato, cantidad) -> {
            Principal2.getCesta().cambiarCantidad(plato, cantidad);
            mostrarCesta(vista);
        }, (menu, cantidad) -> {
            Principal2.getCesta().cambiarCantidad(menu, cantidad);
            mostrarCesta(vista);
        });
    }
}
