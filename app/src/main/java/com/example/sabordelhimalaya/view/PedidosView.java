package com.example.sabordelhimalaya.view;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.LineaPedido;
import com.example.sabordelhimalaya.model.Pedido;
import com.example.sabordelhimalaya.util.Formatters;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public final class PedidosView {

    public interface Listener {
        void onOrderClick(Pedido pedido);
    }

    private final View content;
    private final LayoutInflater inflater;
    private final Listener listener;

    private View state;
    private View progress;
    private TextView stateText;
    private View retry;
    private View empty;
    private LinearLayout list;
    private Runnable retryAction;

    public PedidosView(View content, Listener listener) {
        this.content = content;
        this.listener = listener;
        this.inflater = LayoutInflater.from(content.getContext());
    }

    public void initialize(Runnable onCartaClick, Runnable onCestaClick) {
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets safe = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        state = content.findViewById(R.id.orders_state);
        progress = content.findViewById(R.id.orders_progress);
        stateText = content.findViewById(R.id.orders_state_text);
        retry = content.findViewById(R.id.button_orders_retry);
        empty = content.findViewById(R.id.orders_empty_state);
        list = content.findViewById(R.id.orders_list);

        retry.setOnClickListener(view -> {
            if (retryAction != null) {
                retryAction.run();
            }
        });

        BottomNavigationView navigation = content.findViewById(R.id.orders_navigation);
        navigation.setItemActiveIndicatorEnabled(false);
        navigation.setSelectedItemId(R.id.nav_orders);
        navigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_menu) {
                onCartaClick.run();
            } else if (item.getItemId() == R.id.nav_basket) {
                onCestaClick.run();
            }
            return item.getItemId() == R.id.nav_orders;
        });
        ViewCompat.requestApplyInsets(content);
    }

    public void showLoading() {
        state.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        retry.setVisibility(View.GONE);
        stateText.setText(R.string.orders_loading);
        empty.setVisibility(View.GONE);
        list.setVisibility(View.GONE);
        retryAction = null;
    }

    public void showError(String mensaje, Runnable reintentar) {
        state.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        retry.setVisibility(View.VISIBLE);
        stateText.setText(mensaje);
        empty.setVisibility(View.GONE);
        list.setVisibility(View.GONE);
        retryAction = reintentar;
    }

    public void showEmpty() {
        state.setVisibility(View.GONE);
        empty.setVisibility(View.VISIBLE);
        list.setVisibility(View.GONE);
        list.removeAllViews();
    }

    public void showOrders(List<Pedido> pedidos) {
        state.setVisibility(View.GONE);
        empty.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);
        list.removeAllViews();

        for (Pedido pedido : pedidos) {
            View item = inflater.inflate(R.layout.item_pedido, list, false);
            ((TextView) item.findViewById(R.id.order_number)).setText(
                    content.getContext().getString(R.string.orders_order_number,
                            pedido.getNumeroPedido()));
            ((TextView) item.findViewById(R.id.order_date)).setText(
                    Formatters.dateTime(pedido.getFechaHora()));

            TextView estado = item.findViewById(R.id.order_status);
            estado.setText(Formatters.estado(pedido.getEstado()));
            estado.setTextColor(colorEstado(pedido.getEstado()));

            TextView cancelacion = item.findViewById(R.id.order_cancel_badge);
            String textoCancelacion = Formatters.cancelacion(pedido.getCancelacion());
            if (textoCancelacion == null) {
                cancelacion.setVisibility(View.GONE);
            } else {
                cancelacion.setVisibility(View.VISIBLE);
                cancelacion.setText(textoCancelacion);
            }

            int unidades = 0;
            for (LineaPedido linea : pedido.getItems()) {
                unidades += linea.getCantidad();
            }
            ((TextView) item.findViewById(R.id.order_units)).setText(
                    content.getContext().getString(R.string.orders_units, unidades));
            ((TextView) item.findViewById(R.id.order_total)).setText(
                    Formatters.money(pedido.getImporteTotal()));
            item.setOnClickListener(view -> listener.onOrderClick(pedido));
            list.addView(item);
        }
    }

    private int colorEstado(String estado) {
        if ("LISTO".equals(estado) || "ENTREGADO".equals(estado)) {
            return Color.parseColor("#1E7B34");
        }
        if ("EN_PREPARACION".equals(estado)) {
            return Color.parseColor("#B9770E");
        }
        return content.getResources().getColor(R.color.welcome_title, null);
    }
}
