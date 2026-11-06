package com.example.sabordelhimalaya.controller;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;

import com.bumptech.glide.Glide;
import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.CartRepository;
import com.example.sabordelhimalaya.model.Plato;
import com.example.sabordelhimalaya.util.Formatters;
import com.example.sabordelhimalaya.util.ImageUrls;

import java.math.BigDecimal;

public class PlatoDetalle extends BaseActivity {
    public static final String EXTRA_ID = "extra_plato_id";
    public static final String EXTRA_NOMBRE = "extra_plato_nombre";
    public static final String EXTRA_DESCRIPCION = "extra_plato_descripcion";
    public static final String EXTRA_IMAGEN = "extra_plato_imagen";
    public static final String EXTRA_PRECIO = "extra_plato_precio";
    public static final String EXTRA_CATEGORIA = "extra_plato_categoria";

    private Plato plato;
    private int cantidad = 1;
    private TextView cantidadView;
    private com.google.android.material.button.MaterialButton addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_plato_detalle);
        if (!ensureCliente()) {
            return;
        }

        String id = getIntent().getStringExtra(EXTRA_ID);
        String nombre = getIntent().getStringExtra(EXTRA_NOMBRE);
        String descripcion = getIntent().getStringExtra(EXTRA_DESCRIPCION);
        String imagen = getIntent().getStringExtra(EXTRA_IMAGEN);
        String precioTexto = getIntent().getStringExtra(EXTRA_PRECIO);
        String categoria = getIntent().getStringExtra(EXTRA_CATEGORIA);
        if (id == null || id.isEmpty()) {
            finish();
            return;
        }

        BigDecimal precio = BigDecimal.ZERO;
        try {
            if (precioTexto != null) {
                precio = new BigDecimal(precioTexto);
            }
        } catch (NumberFormatException ignored) {
            // El precio se muestra como texto si no se pudo convertir.
        }
        plato = new Plato(id, nombre, descripcion, imagen, 0, categoria, precio, true);

        findViewById(R.id.button_plato_back).setOnClickListener(view -> finish());
        ((TextView) findViewById(R.id.plato_detail_name)).setText(valor(nombre));
        ((TextView) findViewById(R.id.plato_detail_description)).setText(valor(descripcion));
        ((TextView) findViewById(R.id.plato_detail_category)).setText(valor(categoria));
        ((TextView) findViewById(R.id.plato_detail_price)).setText(Formatters.money(precio));
        cantidadView = findViewById(R.id.plato_detail_quantity);
        addButton = findViewById(R.id.button_plato_add);

        ImageView image = findViewById(R.id.plato_detail_image);
        String url = ImageUrls.resolve(imagen);
        if (url == null) {
            image.setImageResource(R.drawable.ic_plato_placeholder);
        } else {
            Glide.with(image)
                    .load(url)
                    .placeholder(R.drawable.ic_plato_placeholder)
                    .error(R.drawable.ic_plato_placeholder)
                    .centerCrop()
                    .into(image);
        }

        findViewById(R.id.button_plato_qty_minus).setOnClickListener(view -> cambiar(-1));
        findViewById(R.id.button_plato_qty_plus).setOnClickListener(view -> cambiar(1));
        addButton.setOnClickListener(view -> anadir());
        actualizar();
    }

    private void cambiar(int delta) {
        int nueva = cantidad + delta;
        if (nueva < 1 || nueva > 20) {
            return;
        }
        cantidad = nueva;
        actualizar();
    }

    private void actualizar() {
        cantidadView.setText(String.valueOf(cantidad));
        BigDecimal total = plato.getPrecio() == null
                ? BigDecimal.ZERO
                : plato.getPrecio().multiply(BigDecimal.valueOf(cantidad));
        addButton.setText(getString(R.string.plato_add_total, cantidad, Formatters.money(total)));
    }

    private void anadir() {
        CartRepository.get(this).addPlato(plato, cantidad);
        Toast.makeText(this, R.string.plato_added, Toast.LENGTH_SHORT).show();
        finish();
    }

    private String valor(String texto) {
        return texto == null || texto.isEmpty() ? "" : texto;
    }
}
