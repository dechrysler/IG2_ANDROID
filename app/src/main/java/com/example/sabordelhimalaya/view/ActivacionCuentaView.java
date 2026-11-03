package com.example.sabordelhimalaya.view;

import android.graphics.Rect;
import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.OneShotPreDrawListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.sabordelhimalaya.R;

public final class ActivacionCuentaView {
    private final View content;

    public ActivacionCuentaView(View content) {
        this.content = content;
    }

    public void initialize() {
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets safe = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            if (windowInsets.isVisible(WindowInsetsCompat.Type.ime())) {
                OneShotPreDrawListener.add(content, this::revealCode);
            }
            return windowInsets;
        });
        content.findViewById(R.id.activation_code).setOnFocusChangeListener((view, focused) -> {
            if (focused) content.post(this::revealCode);
        });
        ViewCompat.requestApplyInsets(content);
    }

    private void revealCode() {
        View code = content.findViewById(R.id.activation_code);
        if (!code.hasFocus()) return;
        int margin = Math.round(16 * content.getResources().getDisplayMetrics().density);
        code.requestRectangleOnScreen(new Rect(0, -margin * 2,
                code.getWidth(), code.getHeight() + margin), true);
    }
}
