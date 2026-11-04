package com.example.sabordelhimalaya.view;

import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.sabordelhimalaya.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public final class PedidosView {
    private final View content;

    public PedidosView(View content) {
        this.content = content;
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
