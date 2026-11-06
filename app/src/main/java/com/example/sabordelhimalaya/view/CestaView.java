package com.example.sabordelhimalaya.view;

import android.view.View;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.NumberFormat;
import java.math.BigDecimal;
import java.util.Map;
import java.util.function.BiConsumer;
import com.example.sabordelhimalaya.model.Plato;
import com.example.sabordelhimalaya.model.MenuDiario;
import java.util.Locale;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.sabordelhimalaya.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public final class CestaView {
    private final View content;

    public CestaView(View content) {
        this.content = content;
    }

    public void mostrarProductos(Map<Plato, Integer> platos, Map<MenuDiario, Integer> menus,
                                 BiConsumer<Plato, Integer> alCambiarCantidad,
                                 BiConsumer<MenuDiario, Integer> alCambiarMenu) {
        boolean vacia = platos.isEmpty() && menus.isEmpty();
        content.findViewById(R.id.basket_summary).setVisibility(vacia ? View.GONE : View.VISIBLE);
        content.findViewById(R.id.basket_empty_state).setVisibility(vacia ? View.VISIBLE : View.GONE);
        LinearLayout lista = content.findViewById(R.id.basket_items);
        lista.removeAllViews();
        lista.setVisibility(vacia ? View.GONE : View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(content.getContext());
        NumberFormat moneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-ES"));
        int numeroArticulos = 0;
        BigDecimal costeTotal = BigDecimal.ZERO;
        for (Map.Entry<Plato, Integer> entrada : platos.entrySet()) {
            Plato plato = entrada.getKey();
            int cantidad = entrada.getValue();
            numeroArticulos += cantidad;
            costeTotal = costeTotal.add(BigDecimal.valueOf(plato.getPrecio())
                    .multiply(BigDecimal.valueOf(cantidad)));
            View fila = inflater.inflate(R.layout.item_plato, lista, false);
            ((TextView) fila.findViewById(R.id.dish_name)).setText(plato.getNombre());
            ((TextView) fila.findViewById(R.id.dish_description)).setText(content.getResources()
                    .getString(R.string.basket_quantity, cantidad));
            ((TextView) fila.findViewById(R.id.dish_price)).setText(content.getResources()
                    .getString(R.string.basket_unit_price, moneda.format(plato.getPrecio())));
            fila.setOnClickListener(view -> new PlatoDialog().editar(content.getContext(), plato,
                    cantidad, nuevaCantidad -> alCambiarCantidad.accept(plato, nuevaCantidad)));
            lista.addView(fila);
        }
        for (Map.Entry<MenuDiario, Integer> entrada : menus.entrySet()) {
            MenuDiario menu = entrada.getKey();
            int cantidad = entrada.getValue();
            numeroArticulos += cantidad;
            costeTotal = costeTotal.add(BigDecimal.valueOf(menu.getPrecio())
                    .multiply(BigDecimal.valueOf(cantidad)));
            View fila = inflater.inflate(R.layout.item_plato, lista, false);
            ((TextView) fila.findViewById(R.id.dish_name)).setText(R.string.category_daily);
            String elecciones = content.getResources().getString(R.string.basket_menu_choices,
                    menu.getEntrantes().get(0).getNombre(), menu.getPrincipales().get(0).getNombre(),
                    menu.getPostres().get(0).getNombre(), menu.getBebidas().get(0).getNombre());
            String cantidadTexto = content.getResources().getString(R.string.basket_quantity, cantidad);
            ((TextView) fila.findViewById(R.id.dish_description)).setText(cantidadTexto + "\n" + elecciones);
            ((TextView) fila.findViewById(R.id.dish_price)).setText(content.getResources()
                    .getString(R.string.basket_unit_price, moneda.format(menu.getPrecio())));
            fila.setOnClickListener(view -> new PlatoDialog().editar(content.getContext(),
                    content.getResources().getString(R.string.category_daily), elecciones,
                    cantidad, nuevaCantidad -> alCambiarMenu.accept(menu, nuevaCantidad)));
            lista.addView(fila);
        }
        TextView articulos = content.findViewById(R.id.basket_total_items);
        articulos.setText(content.getResources().getQuantityString(
                R.plurals.basket_total_items, numeroArticulos, numeroArticulos));
        TextView total = content.findViewById(R.id.basket_total_price);
        total.setText(content.getResources().getString(R.string.basket_total_price,
                moneda.format(costeTotal)));
    }

    public void initialize(Runnable onCartaClick, Runnable onPedidosClick) {
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets safe = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        BottomNavigationView navigation = content.findViewById(R.id.basket_navigation);
        navigation.setItemActiveIndicatorEnabled(false);
        navigation.setSelectedItemId(R.id.nav_basket);
        navigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_menu) {
                onCartaClick.run();
            } else if (item.getItemId() == R.id.nav_orders) {
                onPedidosClick.run();
            }
            return item.getItemId() == R.id.nav_basket;
        });
        ViewCompat.requestApplyInsets(content);
    }
}
