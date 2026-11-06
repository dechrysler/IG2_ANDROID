package com.example.sabordelhimalaya.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;

import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.util.Formatters;

import java.math.BigDecimal;

public class PedidoConfirmado extends BaseActivity {
    public static final String EXTRA_ORDER_ID = "extra_order_id";
    public static final String EXTRA_ORDER_NUMBER = "extra_order_number";
    public static final String EXTRA_TOTAL = "extra_total";
    public static final String EXTRA_ADDRESS = "extra_address";
    public static final String EXTRA_UPDATED = "extra_updated";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_pedido_confirmado);
        if (!ensureCliente()) {
            return;
        }

        String orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        String orderNumber = getIntent().getStringExtra(EXTRA_ORDER_NUMBER);
        String address = getIntent().getStringExtra(EXTRA_ADDRESS);
        boolean updated = getIntent().getBooleanExtra(EXTRA_UPDATED, false);

        if (updated) {
            ((TextView) findViewById(R.id.success_title)).setText(R.string.order_success_updated);
            ((TextView) findViewById(R.id.success_message))
                    .setText(R.string.order_success_updated_message);
        }
        ((TextView) findViewById(R.id.success_order_number)).setText(valor(orderNumber));
        ((TextView) findViewById(R.id.success_address)).setText(valor(address));
        ((TextView) findViewById(R.id.success_total)).setText(formatearTotal(
                getIntent().getStringExtra(EXTRA_TOTAL)));

        findViewById(R.id.button_success_tracking).setOnClickListener(view -> {
            if (orderId != null && !orderId.isEmpty()) {
                Intent intent = new Intent(this, DetallePedido.class);
                intent.putExtra(DetallePedido.EXTRA_PEDIDO_ID, orderId);
                startActivity(intent);
            }
            finish();
        });
        findViewById(R.id.button_success_continue).setOnClickListener(view -> {
            Intent intent = new Intent(this, Principal2.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private String valor(String texto) {
        return texto == null || texto.isEmpty() ? "—" : texto;
    }

    private String formatearTotal(String total) {
        if (total == null || total.isEmpty()) {
            return "—";
        }
        try {
            return Formatters.money(new BigDecimal(total));
        } catch (NumberFormatException ignored) {
            return total;
        }
    }
}
