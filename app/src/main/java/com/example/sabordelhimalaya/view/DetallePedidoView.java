package com.example.sabordelhimalaya.view;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.LineaPedido;
import com.example.sabordelhimalaya.model.Pedido;
import com.example.sabordelhimalaya.util.Formatters;
import com.google.android.material.button.MaterialButton;

import java.time.LocalDateTime;

public final class DetallePedidoView {
    private final View content;
    private final LayoutInflater inflater;

    private View state;
    private View progress;
    private TextView stateText;
    private View retry;
    private View form;
    private TextView number;
    private TextView date;
    private TextView updated;
    private TextView stepReceived;
    private TextView stepPreparing;
    private TextView stepReady;
    private TextView stepDelivered;
    private TextView cancelBadge;
    private TextView observations;
    private TextView eta;
    private TextView deliveryTime;
    private LinearLayout lines;
    private TextView address;
    private TextView total;
    private MaterialButton modify;
    private MaterialButton cancel;

    private Runnable retryAction;

    public DetallePedidoView(View content) {
        this.content = content;
        this.inflater = LayoutInflater.from(content.getContext());
    }

    public void initialize(Runnable onBack, Runnable onModify, Runnable onCancel) {
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets safe = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        state = content.findViewById(R.id.detail_state);
        progress = content.findViewById(R.id.detail_progress);
        stateText = content.findViewById(R.id.detail_state_text);
        retry = content.findViewById(R.id.button_detail_retry);
        form = content.findViewById(R.id.detail_form);
        number = content.findViewById(R.id.detail_number);
        date = content.findViewById(R.id.detail_date);
        updated = content.findViewById(R.id.detail_updated);
        stepReceived = content.findViewById(R.id.step_received);
        stepPreparing = content.findViewById(R.id.step_preparing);
        stepReady = content.findViewById(R.id.step_ready);
        stepDelivered = content.findViewById(R.id.step_delivered);
        cancelBadge = content.findViewById(R.id.detail_cancel_badge);
        observations = content.findViewById(R.id.detail_observations);
        eta = content.findViewById(R.id.detail_eta);
        deliveryTime = content.findViewById(R.id.detail_delivery_time);
        lines = content.findViewById(R.id.detail_lines);
        address = content.findViewById(R.id.detail_address);
        total = content.findViewById(R.id.detail_total);
        modify = content.findViewById(R.id.button_modify_order);
        cancel = content.findViewById(R.id.button_cancel_order);

        content.findViewById(R.id.button_detail_back).setOnClickListener(view -> onBack.run());
        retry.setOnClickListener(view -> {
            if (retryAction != null) {
                retryAction.run();
            }
        });
        modify.setOnClickListener(view -> onModify.run());
        cancel.setOnClickListener(view -> onCancel.run());
        ViewCompat.requestApplyInsets(content);
    }

    public void showLoading(boolean conservarDatos) {
        if (!conservarDatos) {
            form.setVisibility(View.GONE);
        }
        state.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        retry.setVisibility(View.GONE);
        stateText.setText(R.string.detail_loading);
        retryAction = null;
    }

    public void showError(String mensaje, Runnable reintentar, boolean conservarDatos) {
        if (!conservarDatos) {
            form.setVisibility(View.GONE);
        }
        state.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        retry.setVisibility(View.VISIBLE);
        stateText.setText(mensaje);
        retryAction = reintentar;
    }

    public void render(Pedido pedido) {
        state.setVisibility(View.GONE);
        form.setVisibility(View.VISIBLE);

        number.setText(pedido.getNumeroPedido());
        date.setText(Formatters.dateTime(pedido.getFechaHora()));
        if (pedido.getFechaModificacion() != null
                && pedido.getFechaHora() != null
                && pedido.getFechaModificacion().isAfter(pedido.getFechaHora())) {
            updated.setVisibility(View.VISIBLE);
            updated.setText(content.getContext().getString(R.string.detail_updated,
                    Formatters.dateTime(pedido.getFechaModificacion())));
        } else {
            updated.setVisibility(View.GONE);
        }

        pintarPasos(Formatters.estadoPaso(pedido.getEstado()));

        String textoCancelacion = Formatters.cancelacion(pedido.getCancelacion());
        if (textoCancelacion == null) {
            cancelBadge.setVisibility(View.GONE);
        } else {
            cancelBadge.setVisibility(View.VISIBLE);
            cancelBadge.setText(textoCancelacion);
        }

        if (pedido.getObservacionesCancelacion() == null
                || pedido.getObservacionesCancelacion().isEmpty()) {
            observations.setVisibility(View.GONE);
        } else {
            observations.setVisibility(View.VISIBLE);
            observations.setText(content.getContext().getString(R.string.detail_observations,
                    pedido.getObservacionesCancelacion()));
        }

        if (pedido.getEstimacionLlegada() == null) {
            eta.setVisibility(View.GONE);
        } else {
            eta.setVisibility(View.VISIBLE);
            if (pedido.getEstimacionLlegada().isBefore(LocalDateTime.now())) {
                eta.setText(R.string.detail_eta_passed);
            } else {
                eta.setText(content.getContext().getString(R.string.detail_eta,
                        Formatters.dateTime(pedido.getEstimacionLlegada())));
            }
        }

        if (pedido.getFechaEntrega() == null) {
            deliveryTime.setVisibility(View.GONE);
        } else {
            deliveryTime.setVisibility(View.VISIBLE);
            deliveryTime.setText(content.getContext().getString(R.string.detail_delivery_time,
                    Formatters.dateTime(pedido.getFechaEntrega())));
        }

        lines.removeAllViews();
        for (LineaPedido linea : pedido.getItems()) {
            View row = inflater.inflate(R.layout.item_linea_pedido, lines, false);
            ((TextView) row.findViewById(R.id.line_name)).setText(linea.getNombre());
            ((TextView) row.findViewById(R.id.line_total)).setText(
                    Formatters.money(linea.getImporteLinea()));
            ((TextView) row.findViewById(R.id.line_detail)).setText(
                    linea.getCantidad() + " × " + Formatters.money(linea.getPrecioUnitario())
                            + " · " + Formatters.tipoLinea(linea.getTipo()));
            TextView componentes = row.findViewById(R.id.line_components);
            if (linea.getSeleccion() != null) {
                componentes.setVisibility(View.VISIBLE);
                componentes.setText(content.getContext().getString(
                        R.string.detail_menu_selection,
                        nombre(linea.getSeleccion().getEntrante()),
                        nombre(linea.getSeleccion().getPrincipal()),
                        nombre(linea.getSeleccion().getPostre()),
                        nombre(linea.getSeleccion().getBebida())));
            } else if (linea.getComponentes() == null || linea.getComponentes().isEmpty()) {
                componentes.setVisibility(View.GONE);
            } else {
                componentes.setVisibility(View.VISIBLE);
                componentes.setText(String.join(" · ", linea.getComponentes()));
            }
            lines.addView(row);
        }

        address.setText(pedido.getDireccionEntrega());
        total.setText(Formatters.money(pedido.getImporteTotal()));

        boolean cancelado = Formatters.estaCancelado(pedido.getCancelacion());
        modify.setVisibility(pedido.isModificable() && !cancelado ? View.VISIBLE : View.GONE);
        cancel.setVisibility(pedido.isCancelable() && !cancelado ? View.VISIBLE : View.GONE);
    }

    public void setBusy(boolean busy) {
        modify.setEnabled(!busy);
        cancel.setEnabled(!busy);
        modify.setAlpha(busy ? 0.6f : 1f);
        cancel.setAlpha(busy ? 0.6f : 1f);
    }

    private void pintarPasos(int pasoActual) {
        TextView[] pasos = {stepReceived, stepPreparing, stepReady, stepDelivered};
        int activo = content.getResources().getColor(R.color.welcome_button, null);
        int inactivo = Color.WHITE;
        int textoActivo = Color.WHITE;
        int textoInactivo = content.getResources().getColor(R.color.registration_text, null);
        for (int i = 0; i < pasos.length; i++) {
            boolean completado = i <= pasoActual;
            pasos[i].setBackgroundTintList(ColorStateList.valueOf(completado ? activo : inactivo));
            pasos[i].setTextColor(completado ? textoActivo : textoInactivo);
        }
    }

    private String nombre(com.example.sabordelhimalaya.model.OpcionSeleccionada opcion) {
        return opcion == null || opcion.getNombre() == null ? "—" : opcion.getNombre();
    }
}
