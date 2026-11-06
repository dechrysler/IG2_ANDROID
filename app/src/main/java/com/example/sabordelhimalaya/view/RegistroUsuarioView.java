package com.example.sabordelhimalaya.view;

import android.view.View;
import android.graphics.Rect;
import android.widget.EditText;
import androidx.core.graphics.Insets;
import androidx.core.view.OneShotPreDrawListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.sabordelhimalaya.R;
import com.google.android.material.textfield.TextInputLayout;

public final class RegistroUsuarioView {
    private final View content;

    public RegistroUsuarioView(View content) {
        this.content = content;
    }

    public void applySafeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets safe = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            if (windowInsets.isVisible(WindowInsetsCompat.Type.ime())) {
                OneShotPreDrawListener.add(content, this::revealFocusedField);
            }
            return windowInsets;
        });
        int[] fields = {R.id.registration_name, R.id.registration_username,
                R.id.registration_email, R.id.registration_password, R.id.registration_address};
        for (int fieldId : fields) {
            content.findViewById(fieldId).setOnFocusChangeListener((view, hasFocus) -> {
                if (hasFocus) {
                    content.post(this::revealFocusedField);
                }
            });
        }
        ViewCompat.requestApplyInsets(content);
    }

    private void revealFocusedField() {
        View focused = content.findFocus();
        if (focused == null || !focused.onCheckIsTextEditor()) {
            return;
        }
        int margin = Math.round(16 * content.getResources().getDisplayMetrics().density);
        Rect visibleArea = new Rect(0, -margin * 2, focused.getWidth(), focused.getHeight() + margin);
        focused.requestRectangleOnScreen(visibleArea, true);
    }

    public String[] recogerDatosUsuario() {
        String[] datosUsuario = new String[5];
        int[] fields = {R.id.registration_name, R.id.registration_username,
                R.id.registration_email, R.id.registration_password, R.id.registration_address};
        for (int i = 0; i < fields.length; i++) {
            EditText campo = content.findViewById(fields[i]);
            datosUsuario[i] = campo.getText().toString();
        }
        return datosUsuario;
    }

    public void mostrarErrorCorreo() {
        TextInputLayout container = content.findViewById(R.id.registration_email_container);
        container.setError(content.getContext().getString(R.string.registration_invalid_email));
        EditText correo = container.getEditText();
        if (correo != null) {
            correo.requestFocus();
            OneShotPreDrawListener.add(content, this::revealFocusedField);
        }
    }

    public void mostrarCamposFaltantes(boolean[] camposFaltantes) {
        int[] containers = {R.id.registration_name_container, R.id.registration_username_container,
                R.id.registration_email_container, R.id.registration_password_container,
                R.id.registration_address_container};
        EditText primerCampoFaltante = null;
        for (int i = 0; i < containers.length; i++) {
            TextInputLayout container = content.findViewById(containers[i]);
            container.setError(camposFaltantes[i]
                    ? content.getContext().getString(R.string.registration_required_field) : null);
            if (camposFaltantes[i] && primerCampoFaltante == null) {
                primerCampoFaltante = container.getEditText();
            }
        }
        if (primerCampoFaltante != null) {
            primerCampoFaltante.requestFocus();
            OneShotPreDrawListener.add(content, this::revealFocusedField);
        }
    }
}
