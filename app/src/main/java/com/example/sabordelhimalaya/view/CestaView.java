package com.example.sabordelhimalaya.view;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.CartRepository;
import com.example.sabordelhimalaya.model.dto.OrderItemResponse;
import com.example.sabordelhimalaya.model.dto.OrderPreviewResponse;
import com.example.sabordelhimalaya.util.Formatters;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public final class CestaView {

    public interface Listener {
        void onReview();

        void onConfirm();

        void onCartChanged();
    }

    private final View content;
    private final LayoutInflater inflater;
    private final CartRepository cart;
    private final Listener listener;

    private View emptyState;
    private View form;
    private LinearLayout itemsContainer;
    private TextView units;
    private TextView subtotal;
    private TextInputEditText address;
    private TextInputLayout addressContainer;
    private View previewCard;
    private LinearLayout previewItems;
    private TextView serverTotal;
    private MaterialButton reviewButton;
    private MaterialButton confirmButton;
    private TextView title;

    private boolean suprimirCambioDireccion;

    public CestaView(View content, Listener listener) {
        this.content = content;
        this.listener = listener;
        this.inflater = LayoutInflater.from(content.getContext());
        this.cart = CartRepository.get(content.getContext());
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

        emptyState = content.findViewById(R.id.basket_empty_state);
        form = content.findViewById(R.id.basket_form);
        itemsContainer = content.findViewById(R.id.basket_items);
        units = content.findViewById(R.id.basket_units);
        subtotal = content.findViewById(R.id.basket_subtotal);
        address = content.findViewById(R.id.basket_address);
        addressContainer = content.findViewById(R.id.basket_address_container);
        previewCard = content.findViewById(R.id.basket_preview_card);
        previewItems = content.findViewById(R.id.basket_preview_items);
        serverTotal = content.findViewById(R.id.basket_server_total);
        reviewButton = content.findViewById(R.id.button_review_order);
        confirmButton = content.findViewById(R.id.button_confirm_order);
        title = content.findViewById(R.id.basket_title);

        reviewButton.setOnClickListener(view -> listener.onReview());
        confirmButton.setOnClickListener(view -> listener.onConfirm());

        address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!suprimirCambioDireccion) {
                    addressContainer.setError(null);
                    listener.onCartChanged();
                }
            }
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

    public void setDireccion(String direccion) {
        if (direccion == null || direccion.isEmpty()) {
            return;
        }
        suprimirCambioDireccion = true;
        address.setText(direccion);
        suprimirCambioDireccion = false;
    }

    public String getDireccion() {
        return address.getText() == null ? "" : address.getText().toString().trim();
    }

    public void showDireccionError(String mensaje) {
        addressContainer.setError(mensaje);
    }

    public boolean isPreviewVisible() {
        return previewCard.getVisibility() == View.VISIBLE;
    }

    public void refresh() {
        boolean vacia = cart.isEmpty();
        emptyState.setVisibility(vacia ? View.VISIBLE : View.GONE);
        form.setVisibility(vacia ? View.GONE : View.VISIBLE);
        if (vacia) {
            clearPreview();
            return;
        }

        boolean editando = cart.getEditingOrderId() != null;
        title.setText(editando ? R.string.basket_edit_title : R.string.basket_title);
        confirmButton.setText(editando ? R.string.basket_save_changes : R.string.basket_confirm);
        units.setText(content.getContext().getString(R.string.basket_units, cart.totalUnidades()));
        subtotal.setText(Formatters.money(cart.subtotal()));

        itemsContainer.removeAllViews();
        List<CartRepository.CartItem> items = cart.getItems();
        for (int i = 0; i < items.size(); i++) {
            final int index = i;
            CartRepository.CartItem item = items.get(i);
            View row = inflater.inflate(R.layout.item_cesta, itemsContainer, false);
            ((TextView) row.findViewById(R.id.cart_item_name)).setText(item.nombre);
            TextView componentes = row.findViewById(R.id.cart_item_components);
            if (item.componentes == null || item.componentes.isEmpty()) {
                componentes.setVisibility(View.GONE);
            } else {
                componentes.setVisibility(View.VISIBLE);
                componentes.setText(String.join(" · ", item.componentes));
            }
            ((TextView) row.findViewById(R.id.cart_item_unit_price)).setText(
                    content.getContext().getString(R.string.basket_unit_price,
                            Formatters.money(item.precio)));
            ((TextView) row.findViewById(R.id.cart_item_quantity)).setText(String.valueOf(item.cantidad));
            ((TextView) row.findViewById(R.id.cart_item_total)).setText(
                    Formatters.money(item.precio == null ? null
                            : item.precio.multiply(java.math.BigDecimal.valueOf(item.cantidad))));

            row.findViewById(R.id.button_increase).setOnClickListener(view -> {
                cart.increase(index);
                listener.onCartChanged();
                refresh();
            });
            row.findViewById(R.id.button_decrease).setOnClickListener(view -> {
                cart.decrease(index);
                listener.onCartChanged();
                refresh();
            });
            row.findViewById(R.id.button_remove_item).setOnClickListener(view -> {
                cart.remove(index);
                listener.onCartChanged();
                refresh();
            });
            itemsContainer.addView(row);
        }
    }

    public void showPreview(OrderPreviewResponse preview) {
        previewItems.removeAllViews();
        if (preview.items != null) {
            for (OrderItemResponse item : preview.items) {
                TextView linea = new TextView(content.getContext());
                String componentes = item.componentes == null || item.componentes.isEmpty()
                        ? ""
                        : " (" + String.join(", ", item.componentes) + ")";
                linea.setText(item.cantidad + " × " + item.nombre + componentes + "  —  "
                        + Formatters.money(item.importeLinea));
                linea.setTextColor(content.getResources().getColor(R.color.registration_text, null));
                linea.setTextSize(15);
                linea.setPadding(0, 4, 0, 4);
                previewItems.addView(linea);
            }
        }
        serverTotal.setText(Formatters.money(preview.importeTotal));
        previewCard.setVisibility(View.VISIBLE);
        confirmButton.setEnabled(true);
        confirmButton.setAlpha(1f);
    }

    public void clearPreview() {
        previewCard.setVisibility(View.GONE);
        previewItems.removeAllViews();
        serverTotal.setText("");
        confirmButton.setEnabled(false);
        confirmButton.setAlpha(0.5f);
    }

    public void setBusy(boolean busy) {
        reviewButton.setEnabled(!busy);
        reviewButton.setAlpha(busy ? 0.6f : 1f);
        if (busy) {
            confirmButton.setEnabled(false);
            confirmButton.setAlpha(0.6f);
        } else {
            boolean preview = isPreviewVisible();
            confirmButton.setEnabled(preview);
            confirmButton.setAlpha(preview ? 1f : 0.5f);
        }
    }
}
