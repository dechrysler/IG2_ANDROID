package com.example.sabordelhimalaya.controller;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;

import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.CartRepository;
import com.example.sabordelhimalaya.model.Menu;
import com.example.sabordelhimalaya.model.SeleccionMenu;
import com.example.sabordelhimalaya.model.dto.MenuResponse;
import com.example.sabordelhimalaya.util.ApiErrors;
import com.example.sabordelhimalaya.view.MenuDetalleView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuDetalle extends BaseActivity {
    public static final String EXTRA_MENU_ID = "extra_menu_id";
    public static final String EXTRA_MENU_NAME = "extra_menu_name";

    private MenuDetalleView view;
    private String menuId;
    private Menu menu;
    private boolean cargando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_menu_detalle);
        if (!ensureCliente()) {
            return;
        }
        menuId = getIntent().getStringExtra(EXTRA_MENU_ID);
        if (menuId == null || menuId.isEmpty()) {
            finish();
            return;
        }
        view = new MenuDetalleView(findViewById(R.id.menu_detail_content));
        view.initialize(this::finish, this::anadir);
        cargar();
    }

    private void cargar() {
        if (cargando) {
            return;
        }
        cargando = true;
        view.showLoading();
        api().menu(menuId).enqueue(new Callback<MenuResponse>() {
            @Override
            public void onResponse(Call<MenuResponse> call, Response<MenuResponse> response) {
                cargando = false;
                if (isFinishing()) {
                    return;
                }
                if (handle401(response)) {
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    String mensaje = response.code() == 404
                            ? getString(R.string.menu_detail_unavailable)
                            : ApiErrors.message(response);
                    view.showError(mensaje, MenuDetalle.this::cargar);
                    return;
                }
                menu = Menu.from(response.body());
                view.render(menu);
            }

            @Override
            public void onFailure(Call<MenuResponse> call, Throwable t) {
                cargando = false;
                if (isFinishing()) {
                    return;
                }
                view.showError(ApiErrors.networkMessage(t), MenuDetalle.this::cargar);
            }
        });
    }

    private void anadir() {
        if (menu == null) {
            return;
        }
        SeleccionMenu seleccion = view.getSeleccion();
        if (seleccion == null) {
            showMessage(getString(R.string.menu_selection_incomplete));
            return;
        }
        CartRepository.get(this).addMenu(menu, seleccion, view.getCantidad());
        Toast.makeText(this, R.string.menu_detail_added, Toast.LENGTH_SHORT).show();
        finish();
    }
}
