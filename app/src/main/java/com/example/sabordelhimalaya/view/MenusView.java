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

import java.util.List;

public final class MenusView {

    public interface Listener {
        void onMenuClick(Menu menu);

        void onAddMenu(Menu menu);
    }

    private final View content;
    private final LayoutInflater inflater;
    private final Listener listener;

    private View state;
    private View progress;
    private TextView stateText;
    private View retry;
    private View empty;
    private View emptyAction;
    private LinearLayout list;
    private Runnable retryAction;

    public MenusView(View content, Listener listener) {
        this.content = content;
        this.listener = listener;
        this.inflater = LayoutInflater.from(content.getContext());
    }

    public void initialize(Runnable onBack) {
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets safe = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        content.findViewById(R.id.button_menus_back).setOnClickListener(view -> onBack.run());
        state = content.findViewById(R.id.menus_state);
        progress = content.findViewById(R.id.menus_progress);
        stateText = content.findViewById(R.id.menus_state_text);
        retry = content.findViewById(R.id.button_menus_retry);
        empty = content.findViewById(R.id.menus_empty);
        emptyAction = content.findViewById(R.id.button_menus_empty_action);
        list = content.findViewById(R.id.menus_list);
        retry.setOnClickListener(view -> {
            if (retryAction != null) {
                retryAction.run();
            }
        });
        emptyAction.setOnClickListener(view -> onBack.run());
        ViewCompat.requestApplyInsets(content);
    }

    public void showLoading() {
        state.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        retry.setVisibility(View.GONE);
        stateText.setText(R.string.menus_loading);
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

    public void showMenus(List<Menu> menus) {
        state.setVisibility(View.GONE);
        empty.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);
        list.removeAllViews();

        for (Menu menu : menus) {
            View card = inflater.inflate(R.layout.item_menu_card, list, false);
            ((TextView) card.findViewById(R.id.menu_card_name)).setText(menu.getNombre());

            TextView descripcion = card.findViewById(R.id.menu_card_description);
            if (menu.getDescripcion() == null || menu.getDescripcion().isEmpty()) {
                descripcion.setVisibility(View.GONE);
            } else {
                descripcion.setText(menu.getDescripcion());
            }

            ((TextView) card.findViewById(R.id.menu_card_products)).setText(
                    content.getContext().getString(R.string.menus_products_count,
                            menu.getProductos().size()));
            ((TextView) card.findViewById(R.id.menu_card_price)).setText(
                    Formatters.money(menu.getPrecio()));

            card.findViewById(R.id.menu_card_daily_badge).setVisibility(
                    esMenuDelDia(menu) ? View.VISIBLE : View.GONE);

            ImageView image = card.findViewById(R.id.menu_card_image);
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

            card.setOnClickListener(view -> listener.onMenuClick(menu));
            ((MaterialButton) card.findViewById(R.id.button_menu_card_view))
                    .setOnClickListener(view -> listener.onMenuClick(menu));
            card.findViewById(R.id.button_menu_card_add)
                    .setOnClickListener(view -> listener.onAddMenu(menu));
            list.addView(card);
        }
    }

    private boolean esMenuDelDia(Menu menu) {
        if (menu.getFechaMenuDelDia() == null) {
            return false;
        }
        return menu.getFechaMenuDelDia().equals(java.time.LocalDate.now());
    }

    public static String nombresProductos(Menu menu) {
        StringBuilder builder = new StringBuilder();
        for (Plato plato : menu.getProductos()) {
            if (builder.length() > 0) {
                builder.append(" · ");
            }
            builder.append(plato.getNombre());
        }
        return builder.toString();
    }
}
