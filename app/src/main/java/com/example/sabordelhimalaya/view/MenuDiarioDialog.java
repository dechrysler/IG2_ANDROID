package com.example.sabordelhimalaya.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.MenuDiario;
import com.example.sabordelhimalaya.model.Plato;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import java.util.function.Consumer;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

final class MenuDiarioDialog {
    void mostrar(View carta, MenuDiario menu, Consumer<MenuDiario> alAnadir) {
        Context context = carta.getContext();
        View contenido = LayoutInflater.from(context).inflate(R.layout.dialog_menu_diario, null);
        TextView precio = contenido.findViewById(R.id.daily_dialog_price);
        precio.setText(NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-ES"))
                .format(menu.getPrecio()));
        LinearLayout grupos = contenido.findViewById(R.id.daily_dialog_groups);
        List<RadioGroup> opciones = List.of(
                agregarGrupo(grupos, R.string.category_starters, menu.getEntrantes(), false),
                agregarGrupo(grupos, R.string.daily_mains, menu.getPrincipales(), false),
                agregarGrupo(grupos, R.string.daily_desserts, menu.getPostres(), false),
                agregarGrupo(grupos, R.string.daily_drinks, menu.getBebidas(), true));
        View anadir = contenido.findViewById(R.id.daily_dialog_add);
        anadir.setEnabled(false);
        for (RadioGroup grupo : opciones) {
            grupo.setOnCheckedChangeListener((radioGroup, id) -> anadir.setEnabled(
                    opciones.stream().allMatch(opcion -> opcion.getCheckedRadioButtonId() != View.NO_ID)));
        }

        AlertDialog dialogo = new MaterialAlertDialogBuilder(context)
                .setBackground(ContextCompat.getDrawable(context, R.drawable.dish_dialog_background))
                .setView(contenido)
                .create();
        contenido.findViewById(R.id.daily_dialog_cancel).setOnClickListener(view -> dialogo.dismiss());
        anadir.setOnClickListener(view -> {
            if (!anadir.isEnabled()) return;
            MenuDiario seleccionado = new MenuDiario(menu.getFecha(),
                    List.of(seleccion(opciones.get(0))), List.of(seleccion(opciones.get(1))),
                    List.of(seleccion(opciones.get(2))), List.of(seleccion(opciones.get(3))),
                    menu.getPrecio());
            alAnadir.accept(seleccionado);
            dialogo.dismiss();
        });
        dialogo.show();
    }

    private Plato seleccion(RadioGroup grupo) {
        return (Plato) grupo.findViewById(grupo.getCheckedRadioButtonId()).getTag();
    }

    private RadioGroup agregarGrupo(LinearLayout grupos, int titulo, List<Plato> opciones, boolean bebidas) {
        Context context = grupos.getContext();
        View seccion = LayoutInflater.from(context).inflate(R.layout.item_opciones_menu, grupos, false);
        ((TextView) seccion.findViewById(R.id.daily_group_title)).setText(titulo);
        ((TextView) seccion.findViewById(R.id.daily_group_hint)).setText(bebidas
                ? context.getString(R.string.daily_choose_drink)
                : context.getString(R.string.daily_choose_option, opciones.size()));
        RadioGroup radios = seccion.findViewById(R.id.daily_group_options);
        int oscuro = ContextCompat.getColor(context, R.color.welcome_title);
        int acento = ContextCompat.getColor(context, R.color.carta_accent);
        ColorStateList colores = new ColorStateList(
                new int[][] { new int[] { android.R.attr.state_checked }, new int[0] },
                new int[] { acento, oscuro });
        for (Plato plato : opciones) {
            MaterialRadioButton opcion = new MaterialRadioButton(context);
            opcion.setId(View.generateViewId());
            opcion.setTag(plato);
            opcion.setText(plato.getNombre());
            opcion.setTextColor(oscuro);
            opcion.setTextSize(16);
            opcion.setTypeface(Typeface.SERIF);
            opcion.setButtonTintList(colores);
            opcion.setMinHeight(Math.round(48 * context.getResources().getDisplayMetrics().density));
            radios.addView(opcion, new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT));
        }
        grupos.addView(seccion);
        return radios;
    }
}
