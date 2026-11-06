package com.example.sabordelhimalaya.view;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.Plato;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.function.IntConsumer;

public final class PlatoDialog {
    private static final int CANTIDAD_MAXIMA = 999;
    private int cantidad;
    private boolean actualizando;

    public void mostrar(Context context, Plato plato, IntConsumer alAnadir) {
        mostrar(context, plato.getNombre(), plato.getDescripcion(), 0, alAnadir, false);
    }

    public void editar(Context context, Plato plato, int cantidadInicial, IntConsumer alCambiar) {
        editar(context, plato.getNombre(), plato.getDescripcion(), cantidadInicial, alCambiar);
    }

    public void editar(Context context, String nombre, String descripcion, int cantidadInicial,
                       IntConsumer alCambiar) {
        mostrar(context, nombre, descripcion, cantidadInicial, alCambiar, true);
    }

    private void mostrar(Context context, String nombre, String descripcion, int cantidadInicial,
                         IntConsumer alConfirmar, boolean editar) {
        cantidad = cantidadInicial;
        View dialogContent = LayoutInflater.from(context).inflate(R.layout.dialog_plato, null);
        ((TextView) dialogContent.findViewById(R.id.dialog_dish_title)).setText(nombre);
        ((TextView) dialogContent.findViewById(R.id.dialog_dish_description)).setText(descripcion);
        EditText numero = dialogContent.findViewById(R.id.dialog_dish_quantity);
        View menos = dialogContent.findViewById(R.id.dialog_dish_minus);
        View mas = dialogContent.findViewById(R.id.dialog_dish_plus);
        View anadir = dialogContent.findViewById(R.id.dialog_dish_add);
        TextView volver = dialogContent.findViewById(R.id.dialog_dish_back);
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setBackground(ContextCompat.getDrawable(context, R.drawable.dish_dialog_background))
                .setView(dialogContent)
                .create();
        Runnable actualizar = () -> {
            actualizarEstado(numero, menos, mas, anadir);
            if (editar) {
                alConfirmar.accept(cantidad);
                if (cantidad == 0) dialog.dismiss();
            }
        };
        int limiteEntrada = Math.max(CANTIDAD_MAXIMA, cantidadInicial);
        numero.setFilters(new InputFilter[] {(source, start, end, dest, dstart, dend) -> {
            String resultado = dest.subSequence(0, dstart).toString()
                    + source.subSequence(start, end) + dest.subSequence(dend, dest.length());
            if (resultado.isEmpty()) return null;
            try {
                int valor = Integer.parseInt(resultado);
                return valor >= 0 && valor <= limiteEntrada ? null : "";
            } catch (NumberFormatException exception) {
                return "";
            }
        }});
        numero.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable text) {
                if (actualizando) return;
                if (editar && text.length() == 0) return;
                cantidad = text.length() == 0 ? 0 : Integer.parseInt(text.toString());
                actualizar.run();
            }
        });
        mas.setOnClickListener(view -> {
            if (cantidad < CANTIDAD_MAXIMA) cantidad++;
            actualizarNumero(numero);
            actualizar.run();
        });
        menos.setOnClickListener(view -> {
            cantidad = Math.max(0, cantidad - 1);
            actualizarNumero(numero);
            actualizar.run();
        });
        volver.setOnClickListener(view -> dialog.dismiss());
        if (editar) {
            volver.setText(R.string.action_cancel);
            anadir.setVisibility(View.GONE);
            actualizarNumero(numero);
        }
        anadir.setOnClickListener(view -> {
            if (cantidad > 0) {
                alConfirmar.accept(cantidad);
                dialog.dismiss();
            }
        });
        actualizarEstado(numero, menos, mas, anadir);
        dialog.show();
    }

    private void actualizarNumero(EditText numero) {
        actualizando = true;
        numero.setText(Integer.toString(cantidad));
        actualizando = false;
    }

    private void actualizarEstado(EditText numero, View menos, View mas, View anadir) {
        int visibilidad = cantidad > 0 ? View.VISIBLE : View.GONE;
        numero.setVisibility(visibilidad);
        menos.setVisibility(visibilidad);
        anadir.setEnabled(cantidad > 0);
        mas.setEnabled(cantidad < CANTIDAD_MAXIMA);
        if (cantidad == 0 && numero.hasFocus()) {
            numero.clearFocus();
            mas.requestFocus();
        }
    }
}
