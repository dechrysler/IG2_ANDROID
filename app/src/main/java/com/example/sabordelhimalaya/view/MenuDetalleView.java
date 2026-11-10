package com.example.sabordelhimalaya.view;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.Menu;
import com.example.sabordelhimalaya.model.OpcionSeleccionada;
import com.example.sabordelhimalaya.model.Plato;
import com.example.sabordelhimalaya.model.SeleccionMenu;
import com.example.sabordelhimalaya.util.Formatters;
import com.example.sabordelhimalaya.util.ImageUrls;
import com.google.android.material.button.MaterialButton;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class MenuDetalleView {
    private static final int TIPO_ENTRANTE = 1;
    private static final int TIPO_PRINCIPAL = 2;
    private static final int TIPO_POSTRE = 3;
    private static final int TIPO_BEBIDA = 4;

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
    private LinearLayout sections;
    private TextView selectionHint;
    private TextView quantity;
    private TextView lineTotal;
    private MaterialButton addButton;

    private Menu menu;
    private int cantidad = 1;
    private Plato entranteSeleccionado;
    private Plato principalSeleccionado;
    private Plato postreSeleccionado;
    private Plato bebidaSeleccionada;
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
        sections = content.findViewById(R.id.menu_detail_products);
        selectionHint = content.findViewById(R.id.menu_detail_selection_hint);
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
            if (this.onAdd != null && seleccionCompleta()) {
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
        entranteSeleccionado = null;
        principalSeleccionado = null;
        postreSeleccionado = null;
        bebidaSeleccionada = null;

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

        List<Plato> todos = menu.getTodosLosPlatos();
        String imagen = todos.isEmpty() ? null : todos.get(0).getImagenUrl();
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

        renderSecciones();
        actualizarCantidad();
    }

    public int getCantidad() {
        return cantidad;
    }

    public SeleccionMenu getSeleccion() {
        if (!seleccionCompleta()) {
            return null;
        }
        return SeleccionMenu.from(
                opcion(entranteSeleccionado),
                opcion(principalSeleccionado),
                opcion(postreSeleccionado),
                opcion(bebidaSeleccionada)
        );
    }

    public boolean seleccionCompleta() {
        return entranteSeleccionado != null
                && principalSeleccionado != null
                && postreSeleccionado != null
                && bebidaSeleccionada != null;
    }

    private void renderSecciones() {
        sections.removeAllViews();
        if (menu == null) {
            return;
        }
        agregarSeccion(R.string.menu_section_entrantes, menu.getEntrantes(), TIPO_ENTRANTE);
        agregarSeccion(R.string.menu_section_principales, menu.getPrincipales(), TIPO_PRINCIPAL);
        agregarSeccion(R.string.menu_section_postres, menu.getPostres(), TIPO_POSTRE);
        agregarSeccion(R.string.menu_section_bebidas, menu.getBebidas(), TIPO_BEBIDA);
        actualizarEstadoSeleccion();
    }

    private void agregarSeccion(int titulo, List<Plato> opciones, int tipo) {
        View section = inflater.inflate(R.layout.item_menu_seccion, sections, false);
        ((TextView) section.findViewById(R.id.menu_section_title)).setText(titulo);
        LinearLayout contenedor = section.findViewById(R.id.menu_section_options);
        if (opciones.isEmpty()) {
            TextView vacio = new TextView(content.getContext());
            vacio.setText(R.string.menu_detail_unavailable);
            vacio.setTextColor(content.getResources().getColor(R.color.registration_text, null));
            vacio.setPadding(0, 8, 0, 8);
            contenedor.addView(vacio);
        }
        for (Plato plato : opciones) {
            View row = inflater.inflate(R.layout.item_menu_opcion, contenedor, false);
            RadioButton radio = row.findViewById(R.id.menu_option_radio);
            ((TextView) row.findViewById(R.id.menu_option_name)).setText(plato.getNombre());
            TextView detalle = row.findViewById(R.id.menu_option_description);
            if (plato.getDescripcion() == null || plato.getDescripcion().isEmpty()) {
                detalle.setVisibility(View.GONE);
            } else {
                detalle.setText(plato.getDescripcion());
            }
            boolean selected = esSeleccionado(tipo, plato);
            radio.setChecked(selected);
            row.setBackgroundResource(selected
                    ? R.drawable.bg_option_selected
                    : R.drawable.bg_option_normal);
            row.setOnClickListener(view -> {
                seleccionar(tipo, plato);
                renderSecciones();
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = dp(8);
            contenedor.addView(row, params);
        }
        sections.addView(section);
    }

    private void seleccionar(int tipo, Plato plato) {
        switch (tipo) {
            case TIPO_ENTRANTE:
                entranteSeleccionado = plato;
                break;
            case TIPO_PRINCIPAL:
                principalSeleccionado = plato;
                break;
            case TIPO_POSTRE:
                postreSeleccionado = plato;
                break;
            case TIPO_BEBIDA:
                bebidaSeleccionada = plato;
                break;
            default:
                break;
        }
    }

    private boolean esSeleccionado(int tipo, Plato plato) {
        Plato seleccionado = seleccionado(tipo);
        return seleccionado != null && seleccionado.getId().equals(plato.getId());
    }

    private Plato seleccionado(int tipo) {
        switch (tipo) {
            case TIPO_ENTRANTE:
                return entranteSeleccionado;
            case TIPO_PRINCIPAL:
                return principalSeleccionado;
            case TIPO_POSTRE:
                return postreSeleccionado;
            case TIPO_BEBIDA:
                return bebidaSeleccionada;
            default:
                return null;
        }
    }

    private OpcionSeleccionada opcion(Plato plato) {
        return plato == null ? null : new OpcionSeleccionada(plato.getId(), plato.getNombre());
    }

    private void actualizarEstadoSeleccion() {
        boolean completa = seleccionCompleta();
        selectionHint.setVisibility(completa ? View.GONE : View.VISIBLE);
        addButton.setEnabled(completa);
        addButton.setAlpha(completa ? 1f : 0.55f);
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
        actualizarEstadoSeleccion();
    }

    private boolean esMenuDelDia(Menu menu) {
        return menu.getFechaMenuDelDia() != null
                && menu.getFechaMenuDelDia().equals(LocalDate.now());
    }

    private int dp(int value) {
        return Math.round(value * content.getResources().getDisplayMetrics().density);
    }
}
