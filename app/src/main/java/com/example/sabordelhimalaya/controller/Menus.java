package com.example.sabordelhimalaya.controller;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;

import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.Menu;
import com.example.sabordelhimalaya.model.dto.MenuResponse;
import com.example.sabordelhimalaya.util.ApiErrors;
import com.example.sabordelhimalaya.view.MenusView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Menus extends BaseActivity implements MenusView.Listener {
    private MenusView view;
    private boolean cargando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_menus);
        if (!ensureCliente()) {
            return;
        }
        view = new MenusView(findViewById(R.id.menus_content), this);
        view.initialize(this::finish);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ensureCliente() || view == null) {
            return;
        }
        cargar();
    }

    private void cargar() {
        if (cargando) {
            return;
        }
        cargando = true;
        view.showLoading();
        api().availableMenus().enqueue(new Callback<List<MenuResponse>>() {
            @Override
            public void onResponse(Call<List<MenuResponse>> call, Response<List<MenuResponse>> response) {
                cargando = false;
                if (isFinishing()) {
                    return;
                }
                if (handle401(response)) {
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    view.showError(ApiErrors.message(response), Menus.this::cargar);
                    return;
                }
                List<Menu> menus = new ArrayList<>();
                for (MenuResponse item : response.body()) {
                    menus.add(Menu.from(item));
                }
                if (menus.isEmpty()) {
                    view.showEmpty();
                } else {
                    view.showMenus(menus);
                }
            }

            @Override
            public void onFailure(Call<List<MenuResponse>> call, Throwable t) {
                cargando = false;
                if (isFinishing()) {
                    return;
                }
                view.showError(ApiErrors.networkMessage(t), Menus.this::cargar);
            }
        });
    }

    @Override
    public void onMenuClick(Menu menu) {
        Intent intent = new Intent(this, MenuDetalle.class);
        intent.putExtra(MenuDetalle.EXTRA_MENU_ID, menu.getId());
        intent.putExtra(MenuDetalle.EXTRA_MENU_NAME, menu.getNombre());
        startActivity(intent);
    }
}
