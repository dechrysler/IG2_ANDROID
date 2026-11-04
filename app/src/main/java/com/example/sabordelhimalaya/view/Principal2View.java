package com.example.sabordelhimalaya.view;

import android.view.View;
import android.view.ViewGroup;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.EnumMap;
import java.util.Map;
import java.util.Locale;
import androidx.core.graphics.Insets;
import androidx.core.widget.NestedScrollView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.Categoria;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public final class Principal2View {
    private final View content;
    private final Map<Categoria, LinearLayout> categoryContainers = new EnumMap<>(Categoria.class);
    private final Map<Categoria, TextView> categoryTitles = new EnumMap<>(Categoria.class);

    public Principal2View(View content) {
        this.content = content;
    }

    public void initialize(Runnable onCestaClick, Runnable onPedidosClick) {
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets safe = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        LinearLayout categories = content.findViewById(R.id.category_buttons);
        LinearLayout sections = content.findViewById(R.id.category_sections);
        LayoutInflater inflater = LayoutInflater.from(content.getContext());
        Categoria[] values = Categoria.values();
        for (int i = 0; i < values.length; i++) {
            Categoria categoria = values[i];
            MaterialButton button = (MaterialButton) inflater.inflate(
                    R.layout.item_categoria, categories, false);
            button.setId(View.generateViewId());
            String nombre = getCategoryName(categoria);
            button.setText(nombre);
            button.setTag(categoria);
            categories.addView(button);

            View section = inflater.inflate(R.layout.item_seccion_categoria, sections, false);
            section.setTag(categoria);
            TextView title = section.findViewById(R.id.category_section_title);
            title.setText(nombre);
            categoryTitles.put(categoria, title);
            LinearLayout container = section.findViewById(R.id.category_section_content);
            container.setTag(categoria);
            categoryContainers.put(categoria, container);
            sections.addView(section);
        }
        for (int i = 0; i < categories.getChildCount(); i++) {
            View child = categories.getChildAt(i);
            if (child instanceof MaterialButton) {
                MaterialButton button = (MaterialButton) child;
                button.setCheckable(true);
                button.setOnClickListener(view -> {
                    selectCategory(categories, button);
                    View title = button.getId() == R.id.category_daily
                            ? content.findViewById(R.id.daily_title)
                            : categoryTitles.get((Categoria) button.getTag());
                    scrollToTitle(title);
                });
            }
        }
        selectCategory(categories, content.findViewById(R.id.category_daily));
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
        ViewCompat.requestApplyInsets(content);
    }

    public LinearLayout getCategoryContainer(Categoria categoria) {
        return categoryContainers.get(categoria);
    }

    private void scrollToTitle(View title) {
        if (title == null) return;
        NestedScrollView scroll = content.findViewById(R.id.carta_scroll);
        scroll.post(() -> {
            ViewGroup sections = (ViewGroup) scroll.getChildAt(0);
            Rect position = new Rect();
            title.getDrawingRect(position);
            sections.offsetDescendantRectToMyCoords(title, position);
            int margin = Math.round(16 * content.getResources().getDisplayMetrics().density);
            scroll.smoothScrollTo(0, Math.max(0, position.top - margin));
        });
    }

    private String getCategoryName(Categoria categoria) {
        String nombre = categoria.name().toLowerCase(Locale.ROOT);
        return nombre.substring(0, 1).toUpperCase(Locale.ROOT) + nombre.substring(1);
    }

    private void selectCategory(LinearLayout categories, MaterialButton selected) {
        for (int i = 0; i < categories.getChildCount(); i++) {
            View child = categories.getChildAt(i);
            if (child instanceof MaterialButton) {
                ((MaterialButton) child).setChecked(child == selected);
            }
        }
    }
}
