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
    private TextInputEditText address;
    private TextInputLayout addressContainer;
    private View previewCard;
    private LinearLayout previewItems;
    private TextView serverTotal;
    private View checkoutBar;
    private TextView checkoutLabel;
    private TextView checkoutTotal;
    private MaterialButton checkoutButton;
    private MaterialButton editButton;
    private TextView title;

    private boolean suprimirCambioDireccion;
    private boolean busy;
    private String previewTotal = "";

    public CestaView(View content, Listener listener) {
        this.content = content;
        this.listener = listener;
        this.inflater = LayoutInflater.from(content.getContext());
        this.cart = CartRepository.get(content.getContext());
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
        address = content.findViewById(R.id.basket_address);
        addressContainer = content.findViewById(R.id.basket_address_container);
        previewCard = content.findViewById(R.id.basket_preview_card);
        previewItems = content.findViewById(R.id.basket_preview_items);
        serverTotal = content.findViewById(R.id.basket_server_total);
        checkoutBar = content.findViewById(R.id.checkout_bar);
        checkoutLabel = content.findViewById(R.id.checkout_label);
        checkoutTotal = content.findViewById(R.id.checkout_total);
        checkoutButton = content.findViewById(R.id.button_checkout_order);
        editButton = content.findViewById(R.id.button_edit_cart);
        title = content.findViewById(R.id.basket_title);

        checkoutButton.setOnClickListener(view -> {
            if (isPreviewVisible()) {
                listener.onConfirm();
            } else {
                listener.onReview();
            }
        });
        editButton.setOnClickListener(view -> {
            clearPreview();
            ((androidx.core.widget.NestedScrollView) content.findViewById(R.id.basket_scroll))
                    .smoothScrollTo(0, 0);
        });

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
        checkoutBar.setVisibility(vacia ? View.GONE : View.VISIBLE);
        if (vacia) {
            clearPreview();
            return;
        }

        boolean editando = cart.getEditingOrderId() != null;
        title.setText(editando ? R.string.basket_edit_title : R.string.basket_title);
        units.setText(content.getContext().getString(R.string.basket_units, cart.totalUnidades()));

        itemsContainer.removeAllViews();
        List<CartRepository.CartItem> items = cart.getItems();
        for (int i = 0; i < items.size(); i++) {
            final int index = i;
            CartRepository.CartItem item = items.get(i);
            View row = inflater.inflate(R.layout.item_cesta, itemsContainer, false);
            ((TextView) row.findViewById(R.id.cart_item_name)).setText(item.nombre);
            TextView componentes = row.findViewById(R.id.cart_item_components);
            if (item.esMenu() && item.tieneSeleccionCompleta()) {
                componentes.setVisibility(View.VISIBLE);
                componentes.setText(content.getContext().getString(
                        R.string.basket_menu_selection,
                        valor(item.entranteNombre), valor(item.principalNombre),
                        valor(item.postreNombre), valor(item.bebidaNombre)));
            } else if (item.componentes == null || item.componentes.isEmpty()) {
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
        actualizarBarra();
    }

    public void showPreview(OrderPreviewResponse preview) {
        previewItems.removeAllViews();
        if (preview.items != null) {
            for (OrderItemResponse item : preview.items) {
                TextView linea = new TextView(content.getContext());
                String detalle = item.seleccion != null
                        ? " (" + seleccionPreview(item) + ")"
                        : (item.componentes == null || item.componentes.isEmpty()
                        ? ""
                        : " (" + String.join(", ", item.componentes) + ")");
                linea.setText(item.cantidad + " × " + item.nombre + detalle + "  —  "
                        + Formatters.money(item.importeLinea));
                linea.setTextColor(content.getResources().getColor(R.color.registration_text, null));
                linea.setTextSize(15);
                linea.setPadding(0, 4, 0, 4);
                previewItems.addView(linea);
            }
        }
        previewTotal = Formatters.money(preview.importeTotal);
        serverTotal.setText(previewTotal);
        previewCard.setVisibility(View.VISIBLE);
        actualizarBarra();
    }

    public void clearPreview() {
        previewCard.setVisibility(View.GONE);
        previewItems.removeAllViews();
        serverTotal.setText("");
        previewTotal = "";
        actualizarBarra();
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
        if (busy) {
            checkoutButton.setEnabled(false);
            checkoutButton.setText(R.string.basket_processing);
            checkoutButton.setAlpha(0.7f);
            editButton.setEnabled(false);
        } else {
            actualizarBarra();
        }
    }

    private void actualizarBarra() {
        if (checkoutBar == null) {
            return;
        }
        boolean vacia = cart.isEmpty();
        if (vacia) {
            checkoutBar.setVisibility(View.GONE);
            return;
        }
        checkoutBar.setVisibility(View.VISIBLE);
        boolean preview = isPreviewVisible();
        boolean editando = cart.getEditingOrderId() != null;

        checkoutLabel.setText(preview
                ? R.string.basket_checkout_server
                : R.string.basket_checkout_estimated);
        checkoutTotal.setText(preview ? previewTotal : Formatters.money(cart.subtotal()));

        if (busy) {
            checkoutButton.setEnabled(false);
            checkoutButton.setText(R.string.basket_processing);
            checkoutButton.setAlpha(0.7f);
            return;
        }

        checkoutButton.setEnabled(true);
        checkoutButton.setAlpha(1f);
        if (preview) {
            checkoutButton.setText(editando
                    ? R.string.basket_save_changes
                    : R.string.basket_confirm);
        } else {
            checkoutButton.setText(R.string.basket_review);
        }
        editButton.setVisibility(preview ? View.VISIBLE : View.GONE);
        editButton.setEnabled(true);
    }

    private String seleccionPreview(OrderItemResponse item) {
        return content.getContext().getString(R.string.basket_menu_selection,
                nombre(item.seleccion.entrante),
                nombre(item.seleccion.principal),
                nombre(item.seleccion.postre),
                nombre(item.seleccion.bebida));
    }

    private String nombre(OrderItemResponse.Opcion opcion) {
        return opcion == null || opcion.nombre == null ? "—" : opcion.nombre;
    }

    private String valor(String texto) {
        return texto == null || texto.isEmpty() ? "—" : texto;
    }
}
