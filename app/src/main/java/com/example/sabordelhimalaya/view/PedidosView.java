package com.example.sabordelhimalaya.view;

import android.view.View;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.graphics.Insets;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.MenuDiario;
import com.example.sabordelhimalaya.model.Pedido;
import com.example.sabordelhimalaya.model.Plato;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PedidosView {
    private final View content;

    public PedidosView(View content) {
        this.content = content;
    }

    public void mostrarPedidos(List<Pedido> pedidos) {
        TextView recuento = content.findViewById(R.id.orders_count);
        recuento.setText(content.getResources().getQuantityString(R.plurals.orders_count,
                pedidos.size(), pedidos.size()));
        LinearLayout lista = content.findViewById(R.id.orders_list);
        lista.removeAllViews();
        lista.setVisibility(pedidos.isEmpty() ? View.GONE : View.VISIBLE);
        content.findViewById(R.id.orders_empty_state)
                .setVisibility(pedidos.isEmpty() ? View.VISIBLE : View.GONE);
        LayoutInflater inflater = LayoutInflater.from(content.getContext());
        NumberFormat moneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-ES"));
        DateTimeFormatter fecha = DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm");
        for (int i = pedidos.size() - 1; i >= 0; i--) {
            Pedido pedido = pedidos.get(i);
            View fila = inflater.inflate(R.layout.item_pedido_local, lista, false);
            ((TextView) fila.findViewById(R.id.order_card_title)).setText(content.getResources()
                    .getString(R.string.orders_dialog_title, pedido.getIdPedido()));
            ((TextView) fila.findViewById(R.id.order_card_total)).setText(moneda.format(pedido.getPrecioTotal()));
            ((TextView) fila.findViewById(R.id.order_card_date)).setText(fecha.format(pedido.getFecha()));
            ((TextView) fila.findViewById(R.id.order_card_status)).setText(content.getResources()
                    .getString(R.string.order_status_badge, pedido.getEstadoPedido().name().replace('_', ' ')));
            int unidades = pedido.getNumeroElementos();
            ((TextView) fila.findViewById(R.id.order_card_count)).setText(content.getResources()
                    .getQuantityString(R.plurals.basket_total_items, unidades, unidades));
            fila.setOnClickListener(view -> mostrarDetalle(pedido, moneda));
            lista.addView(fila);
        }
    }

    private void mostrarDetalle(Pedido pedido, NumberFormat moneda) {
        View detalle = LayoutInflater.from(content.getContext()).inflate(R.layout.dialog_pedido_local, null);
        ((TextView) detalle.findViewById(R.id.order_dialog_title)).setText(content.getResources()
                .getString(R.string.orders_dialog_title, pedido.getIdPedido()));
        String estado = pedido.getEstadoPedido().name().replace('_', ' ');
        ((TextView) detalle.findViewById(R.id.order_dialog_status)).setText(content.getResources()
                .getString(R.string.order_status_badge, estado));
        mostrarArticulos(detalle.findViewById(R.id.order_dialog_details), pedido);
        int unidades = pedido.getNumeroElementos();
        String articulos = content.getResources().getQuantityString(R.plurals.basket_total_items,
                unidades, unidades);
        ((TextView) detalle.findViewById(R.id.order_dialog_count)).setText(articulos);
        ((TextView) detalle.findViewById(R.id.order_dialog_total)).setText(moneda.format(pedido.getPrecioTotal()));
        AlertDialog dialogo = new MaterialAlertDialogBuilder(content.getContext())
                .setBackground(ContextCompat.getDrawable(content.getContext(), R.drawable.dish_dialog_background))
                .setView(detalle)
                .create();
        detalle.findViewById(R.id.order_dialog_close).setOnClickListener(view -> dialogo.dismiss());
        dialogo.show();
    }

    private void mostrarArticulos(LinearLayout lista, Pedido pedido) {
        LayoutInflater inflater = LayoutInflater.from(lista.getContext());
        Map<String, Integer> cantidades = new LinkedHashMap<>();
        for (Plato plato : pedido.getPlatos()) cantidades.merge(plato.getNombre(), 1, Integer::sum);
        for (Map.Entry<String, Integer> plato : cantidades.entrySet()) {
            View fila = inflater.inflate(R.layout.item_pedido_plato, lista, false);
            ((TextView) fila.findViewById(R.id.order_dish_quantity)).setText(content.getResources()
                    .getString(R.string.order_dish_quantity, plato.getValue()));
            ((TextView) fila.findViewById(R.id.order_dish_name)).setText(plato.getKey());
            lista.addView(fila);
        }
        for (MenuDiario menu : pedido.getMenusDiarios()) {
            View tarjeta = inflater.inflate(R.layout.item_pedido_menu, lista, false);
            ((TextView) tarjeta.findViewById(R.id.order_menu_starter)).setText(nombres(menu.getEntrantes()));
            ((TextView) tarjeta.findViewById(R.id.order_menu_main)).setText(nombres(menu.getPrincipales()));
            ((TextView) tarjeta.findViewById(R.id.order_menu_dessert)).setText(nombres(menu.getPostres()));
            ((TextView) tarjeta.findViewById(R.id.order_menu_drink)).setText(nombres(menu.getBebidas()));
            lista.addView(tarjeta);
        }
    }

    private String nombres(List<Plato> platos) {
        return String.join(", ", platos.stream().map(Plato::getNombre)
                .collect(java.util.stream.Collectors.toList()));
    }

    public void initialize(Runnable onCartaClick, Runnable onCestaClick) {
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets safe = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            return WindowInsetsCompat.CONSUMED;
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
}
