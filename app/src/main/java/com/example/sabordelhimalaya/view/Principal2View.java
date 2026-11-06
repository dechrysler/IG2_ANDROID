package com.example.sabordelhimalaya.view;

import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.Categoria;
import com.example.sabordelhimalaya.model.Menu;
import com.example.sabordelhimalaya.model.Plato;
import com.example.sabordelhimalaya.util.Formatters;
import com.example.sabordelhimalaya.util.ImageUrls;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Principal2View {

    public interface Listener {
        void onAddPlato(Plato plato);

        void onAddMenu(Menu menu);
    }

    private final View content;
    private final LayoutInflater inflater;
    private final Listener listener;
    private final Map<Integer, LinearLayout> categoryContainers = new HashMap<>();
    private final Map<Integer, TextView> categoryTitles = new HashMap<>();

    private LinearLayout categoryButtons;
    private LinearLayout categorySections;
    private FrameLayout dailyContent;
    private View state;
    private View progress;
    private TextView stateText;
    private View retry;
    private MaterialButton dailyButton;
    private Runnable retryAction;

    public Principal2View(View content, Listener listener) {
        this.content = content;
        this.listener = listener;
        this.inflater = LayoutInflater.from(content.getContext());
    }

    public void initialize(Runnable onCestaClick, Runnable onPedidosClick, Runnable onLogoutClick) {
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets safe = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        categoryButtons = content.findViewById(R.id.category_buttons);
        categorySections = content.findViewById(R.id.category_sections);
        dailyContent = content.findViewById(R.id.daily_content);
        state = content.findViewById(R.id.carta_state);
        progress = content.findViewById(R.id.carta_progress);
        stateText = content.findViewById(R.id.carta_state_text);
        retry = content.findViewById(R.id.button_carta_retry);
        dailyButton = content.findViewById(R.id.category_daily);

        dailyButton.setOnClickListener(view -> {
            selectCategory(dailyButton);
            scrollToTitle(content.findViewById(R.id.daily_title));
        });
        retry.setOnClickListener(view -> {
            if (retryAction != null) {
                retryAction.run();
            }
        });
        content.findViewById(R.id.button_logout).setOnClickListener(view -> onLogoutClick.run());

        BottomNavigationView navigation = content.findViewById(R.id.principal_navigation);
        navigation.setItemActiveIndicatorEnabled(false);
        navigation.setSelectedItemId(R.id.nav_menu);
        navigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_basket) {
                onCestaClick.run();
            } else if (item.getItemId() == R.id.nav_orders) {
                onPedidosClick.run();
            }
            return item.getItemId() == R.id.nav_menu;
        });
        selectCategory(dailyButton);
        ViewCompat.requestApplyInsets(content);
    }

    public void showLoading() {
        if (!categoryContainers.isEmpty()) {
            // En una recarga silenciosa se conserva el contenido ya mostrado.
            state.setVisibility(View.GONE);
            return;
        }
        state.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        retry.setVisibility(View.GONE);
        stateText.setText(R.string.carta_loading);
        retryAction = null;
        dailyContent.removeAllViews();
        categorySections.removeAllViews();
    }

    public void showError(String mensaje, Runnable reintentar) {
        state.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        retry.setVisibility(View.VISIBLE);
        stateText.setText(mensaje);
        retryAction = reintentar;
        if (categoryContainers.isEmpty()) {
            dailyContent.removeAllViews();
            categorySections.removeAllViews();
        }
    }

    public void showCategorias(List<Categoria> categorias) {
        state.setVisibility(View.GONE);
        categorySections.removeAllViews();
        categoryContainers.clear();
        categoryTitles.clear();

        // Conserva el botón "Menú diario" y elimina las pestañas anteriores.
        for (int i = categoryButtons.getChildCount() - 1; i >= 0; i--) {
            View child = categoryButtons.getChildAt(i);
            if (child != dailyButton) {
                categoryButtons.removeViewAt(i);
            }
        }

        for (Categoria categoria : categorias) {
            MaterialButton button = (MaterialButton) inflater.inflate(
                    R.layout.item_categoria, categoryButtons, false);
            button.setId(View.generateViewId());
            button.setText(categoria.getNombreVisible());
            button.setCheckable(true);
            button.setTag(categoria.getNumero());
            button.setOnClickListener(view -> {
                selectCategory((MaterialButton) view);
                scrollToTitle(categoryTitles.get((Integer) view.getTag()));
            });
            categoryButtons.addView(button);

            View section = inflater.inflate(R.layout.item_seccion_categoria, categorySections, false);
            TextView title = section.findViewById(R.id.category_section_title);
            title.setText(categoria.getNombreVisible());
            categoryTitles.put(categoria.getNumero(), title);

            LinearLayout container = section.findViewById(R.id.category_section_content);
            TextView cargando = new TextView(content.getContext());
            cargando.setText(R.string.loading);
            cargando.setPadding(dp(12), dp(12), dp(12), dp(12));
            container.addView(cargando);
            categoryContainers.put(categoria.getNumero(), container);
            categorySections.addView(section);
        }
        selectCategory(dailyButton);
    }

    public void showPlatos(int numeroCategoria, List<Plato> platos) {
        LinearLayout container = categoryContainers.get(numeroCategoria);
        if (container == null) {
            return;
        }
        container.removeAllViews();
        if (platos.isEmpty()) {
            TextView vacio = new TextView(content.getContext());
            vacio.setText(R.string.carta_empty_category);
            vacio.setPadding(dp(12), dp(16), dp(12), dp(16));
            vacio.setTextColor(content.getResources().getColor(R.color.registration_text, null));
            container.addView(vacio);
            return;
        }
        for (Plato plato : platos) {
            View item = inflater.inflate(R.layout.item_plato, container, false);
            ((TextView) item.findViewById(R.id.dish_name)).setText(plato.getNombre());
            TextView descripcion = item.findViewById(R.id.dish_description);
            if (plato.getDescripcion() == null || plato.getDescripcion().isEmpty()) {
                descripcion.setVisibility(View.GONE);
            } else {
                descripcion.setText(plato.getDescripcion());
            }
            ((TextView) item.findViewById(R.id.dish_price)).setText(Formatters.money(plato.getPrecio()));
            cargarImagen(item.findViewById(R.id.dish_image), plato.getImagenUrl());
            item.findViewById(R.id.button_add_dish)
                    .setOnClickListener(view -> listener.onAddPlato(plato));
            container.addView(item);
        }
    }

    public void showPlatosError(int numeroCategoria, String mensaje) {
        LinearLayout container = categoryContainers.get(numeroCategoria);
        if (container == null) {
            return;
        }
        container.removeAllViews();
        TextView error = new TextView(content.getContext());
        error.setText(mensaje);
        error.setPadding(dp(12), dp(16), dp(12), dp(16));
        error.setTextColor(content.getResources().getColor(R.color.registration_text, null));
        container.addView(error);
    }

    public void showMenu(Menu menu) {
        dailyContent.removeAllViews();
        if (menu == null) {
            showMenuMessage(content.getContext().getString(R.string.carta_no_menu));
            return;
        }
        View item = inflater.inflate(R.layout.item_menu_hoy, dailyContent, false);
        ((TextView) item.findViewById(R.id.menu_name)).setText(menu.getNombre());
        TextView descripcion = item.findViewById(R.id.menu_description);
        if (menu.getDescripcion() == null || menu.getDescripcion().isEmpty()) {
            descripcion.setVisibility(View.GONE);
        } else {
            descripcion.setText(menu.getDescripcion());
        }
        TextView componentes = item.findViewById(R.id.menu_components);
        String lista = menu.nombresProductos();
        if (lista.isEmpty()) {
            componentes.setVisibility(View.GONE);
        } else {
            componentes.setText(content.getContext()
                    .getString(R.string.carta_menu_components, lista));
        }
        ((TextView) item.findViewById(R.id.menu_price)).setText(Formatters.money(menu.getPrecio()));
        String imagen = menu.getProductos().isEmpty() ? null : menu.getProductos().get(0).getImagenUrl();
        cargarImagen(item.findViewById(R.id.menu_image), imagen);
        item.findViewById(R.id.button_add_menu)
                .setOnClickListener(view -> listener.onAddMenu(menu));
        dailyContent.addView(item);
    }

    public void showMenuMessage(String mensaje) {
        dailyContent.removeAllViews();
        TextView texto = new TextView(content.getContext());
        texto.setText(mensaje);
        texto.setPadding(dp(4), dp(8), dp(4), dp(8));
        texto.setTextColor(content.getResources().getColor(R.color.registration_text, null));
        texto.setTextSize(16);
        dailyContent.addView(texto);
    }

    private void cargarImagen(ImageView imageView, String imagenUrl) {
        String url = ImageUrls.resolve(imagenUrl);
        if (url == null) {
            imageView.setImageResource(R.drawable.ic_plato_placeholder);
            return;
        }
        Glide.with(imageView)
                .load(url)
                .placeholder(R.drawable.ic_plato_placeholder)
                .error(R.drawable.ic_plato_placeholder)
                .centerCrop()
                .into(imageView);
    }

    private void selectCategory(MaterialButton selected) {
        for (int i = 0; i < categoryButtons.getChildCount(); i++) {
            View child = categoryButtons.getChildAt(i);
            if (child instanceof MaterialButton) {
                ((MaterialButton) child).setChecked(child == selected);
            }
        }
    }

    public void mostrarContadorCesta(int cantidad) {
        BottomNavigationView navegacion = content.findViewById(R.id.principal_navigation);
        if (cantidad == 0) {
            navegacion.removeBadge(R.id.nav_basket);
            return;
        }
        BadgeDrawable contador = navegacion.getOrCreateBadge(R.id.nav_basket);
        contador.setNumber(cantidad);
    }

    public void mostrarPlatos(List<Plato> platos, BiConsumer<Plato, Integer> alAnadirPlato) {
        for (LinearLayout container : categoryContainers.values()) {
            container.removeAllViews();
        }
        LayoutInflater inflater = LayoutInflater.from(content.getContext());
        NumberFormat formatoPrecio = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
        formatoPrecio.setCurrency(Currency.getInstance("EUR"));
        for (Plato plato : platos) {
            LinearLayout container = categoryContainers.get(plato.getCategoria());
            if (container == null) continue;
            if (container.getChildCount() > 0) {
                View divider = new View(content.getContext());
                divider.setBackgroundResource(R.color.carta_border);
                int height = Math.max(1, Math.round(content.getResources().getDisplayMetrics().density));
                container.addView(divider, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, height));
            }
            View row = inflater.inflate(R.layout.item_plato, container, false);
            ((TextView) row.findViewById(R.id.dish_name)).setText(plato.getNombre());
            ((TextView) row.findViewById(R.id.dish_description)).setText(plato.getDescripcion());
            ((TextView) row.findViewById(R.id.dish_price)).setText(formatoPrecio.format(plato.getPrecio()));
            row.setOnClickListener(view -> new PlatoDialog().mostrar(content.getContext(), plato,
                    cantidad -> alAnadirPlato.accept(plato, cantidad)));
            container.addView(row);
        }
    }

    public void mostrarErrorCarga() {
        Snackbar.make(content, R.string.dishes_load_error, Snackbar.LENGTH_LONG).show();
    }

    public void configurarMenuDiario(MenuDiario menu, Consumer<MenuDiario> alAnadir) {
        content.findViewById(R.id.daily_content).setOnClickListener(view -> {
            if (menu == null) {
                Snackbar.make(content, R.string.daily_menu_unavailable, Snackbar.LENGTH_SHORT).show();
                return;
            }
            new MenuDiarioDialog().mostrar(content, menu, alAnadir);
        });
    }

    private void scrollToTitle(View title) {
        if (title == null) {
            return;
        }
        NestedScrollView scroll = content.findViewById(R.id.carta_scroll);
        scroll.post(() -> {
            ViewGroup sections = (ViewGroup) scroll.getChildAt(0);
            Rect position = new Rect();
            title.getDrawingRect(position);
            sections.offsetDescendantRectToMyCoords(title, position);
            int margin = dp(16);
            scroll.smoothScrollTo(0, Math.max(0, position.top - margin));
        });
    }

    private int dp(int value) {
        return Math.round(value * content.getResources().getDisplayMetrics().density);
    }
}
