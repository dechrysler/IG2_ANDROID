package com.example.sabordelhimalaya.view;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.Menu;
import com.example.sabordelhimalaya.model.Plato;
import com.example.sabordelhimalaya.util.Formatters;
import com.example.sabordelhimalaya.util.ImageUrls;
import com.google.android.material.button.MaterialButton;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class MenuDetalleView {
    private final View content;
    private final LayoutInflater inflater;

    private View state;
    private View progress;
    private TextView stateText;
    private View retry;
    private View form;
    private ImageView image;
    private TextView badge;
    private TextView name;
    private TextView description;
    private TextView price;
    private LinearLayout products;
    private TextView quantity;
    private TextView lineTotal;
    private MaterialButton addButton;

    private Menu menu;
    private int cantidad = 1;
    private Runnable retryAction;
    private Runnable onAdd;

    public MenuDetalleView(View content) {
        this.content = content;
        this.inflater = LayoutInflater.from(content.getContext());
    }

    public void initialize(Runnable onBack, Runnable onAdd) {
        this.onAdd = onAdd;
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets safe = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        content.findViewById(R.id.button_menu_detail_back).setOnClickListener(view -> onBack.run());
        state = content.findViewById(R.id.menu_detail_state);
        progress = content.findViewById(R.id.menu_detail_progress);
        stateText = content.findViewById(R.id.menu_detail_state_text);
        retry = content.findViewById(R.id.button_menu_detail_retry);
        form = content.findViewById(R.id.menu_detail_form);
        image = content.findViewById(R.id.menu_detail_image);
        badge = content.findViewById(R.id.menu_detail_daily_badge);
        name = content.findViewById(R.id.menu_detail_name);
        description = content.findViewById(R.id.menu_detail_description);
        price = content.findViewById(R.id.menu_detail_price);
        products = content.findViewById(R.id.menu_detail_products);
        quantity = content.findViewById(R.id.menu_detail_quantity);
        lineTotal = content.findViewById(R.id.menu_detail_line_total);
        addButton = content.findViewById(R.id.button_menu_add);

        retry.setOnClickListener(view -> {
            if (retryAction != null) {
                retryAction.run();
            }
        });
        content.findViewById(R.id.button_menu_qty_minus).setOnClickListener(view -> cambiarCantidad(-1));
        content.findViewById(R.id.button_menu_qty_plus).setOnClickListener(view -> cambiarCantidad(1));
        addButton.setOnClickListener(view -> {
            if (this.onAdd != null) {
                this.onAdd.run();
            }
        });
        ViewCompat.requestApplyInsets(content);
    }

    public void showLoading() {
        state.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        retry.setVisibility(View.GONE);
        stateText.setText(R.string.menu_detail_loading);
        form.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);
        retryAction = null;
    }

    public void showError(String mensaje, Runnable reintentar) {
        state.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        retry.setVisibility(View.VISIBLE);
        stateText.setText(mensaje);
        form.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);
        retryAction = reintentar;
    }

    public void render(Menu menu) {
        this.menu = menu;
        cantidad = 1;
        state.setVisibility(View.GONE);
        form.setVisibility(View.VISIBLE);
        addButton.setVisibility(View.VISIBLE);

        name.setText(menu.getNombre());
        if (menu.getDescripcion() == null || menu.getDescripcion().isEmpty()) {
            description.setVisibility(View.GONE);
        } else {
            description.setVisibility(View.VISIBLE);
            description.setText(menu.getDescripcion());
        }
        price.setText(Formatters.money(menu.getPrecio()));
        badge.setVisibility(esMenuDelDia(menu) ? View.VISIBLE : View.GONE);

        String imagen = menu.getProductos().isEmpty()
                ? null : menu.getProductos().get(0).getImagenUrl();
        String url = ImageUrls.resolve(imagen);
        if (url == null) {
            image.setImageResource(R.drawable.ic_plato_placeholder);
        } else {
            Glide.with(image)
                    .load(url)
                    .placeholder(R.drawable.ic_plato_placeholder)
                    .error(R.drawable.ic_plato_placeholder)
                    .centerCrop()
                    .into(image);
        }

        products.removeAllViews();
        for (Plato plato : menu.getProductos()) {
            View row = inflater.inflate(R.layout.item_menu_producto, products, false);
            ((TextView) row.findViewById(R.id.menu_product_name)).setText(plato.getNombre());
            ((TextView) row.findViewById(R.id.menu_product_category)).setText(
                    plato.getCategoriaNombre() == null ? "" : plato.getCategoriaNombre());
            ((TextView) row.findViewById(R.id.menu_product_price)).setText(
                    Formatters.money(plato.getPrecio()));
            products.addView(row);
        }
        actualizarCantidad();
    }

    public int getCantidad() {
        return cantidad;
    }

    private void cambiarCantidad(int delta) {
        int nueva = cantidad + delta;
        if (nueva < 1 || nueva > 20) {
            return;
        }
        cantidad = nueva;
        actualizarCantidad();
    }

    private void actualizarCantidad() {
        quantity.setText(String.valueOf(cantidad));
        if (menu != null) {
            BigDecimal total = menu.getPrecio() == null
                    ? null
                    : menu.getPrecio().multiply(BigDecimal.valueOf(cantidad));
            lineTotal.setText(Formatters.money(total));
        }
        addButton.setText(content.getContext().getString(R.string.menu_detail_add, cantidad));
    }

    private boolean esMenuDelDia(Menu menu) {
        return menu.getFechaMenuDelDia() != null
                && menu.getFechaMenuDelDia().equals(LocalDate.now());
    }
}
