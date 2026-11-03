package com.example.sabordelhimalaya.view;

import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Presentación: el contenido respeta las barras y el fondo ocupa toda la pantalla. */
public final class WelcomeView {
    private final View content;

    public WelcomeView(View content) {
        this.content = content;
    }

    public void applySafeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets safe = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(content);
    }
}
