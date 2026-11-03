package com.example.sabordelhimalaya.view;

import android.graphics.Rect;
import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.OneShotPreDrawListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.sabordelhimalaya.R;

public final class InicioSesionView {
    private final View content;

    public InicioSesionView(View content) {
        this.content = content;
    }

    public void initialize() {
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets safe = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            if (windowInsets.isVisible(WindowInsetsCompat.Type.ime())) {
                OneShotPreDrawListener.add(content, this::revealFocusedField);
            }
            return windowInsets;
        });
        int[] fields = {R.id.login_username, R.id.login_password};
        for (int id : fields) {
            content.findViewById(id).setOnFocusChangeListener((view, focused) -> {
                if (focused) content.post(this::revealFocusedField);
            });
        }
        ViewCompat.requestApplyInsets(content);
    }

    private void revealFocusedField() {
        View focused = content.findFocus();
        if (focused == null || !focused.onCheckIsTextEditor()) return;
        int margin = Math.round(16 * content.getResources().getDisplayMetrics().density);
        focused.requestRectangleOnScreen(new Rect(0, -margin * 2,
                focused.getWidth(), focused.getHeight() + margin), true);
    }
}
