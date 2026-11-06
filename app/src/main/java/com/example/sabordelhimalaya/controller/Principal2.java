package com.example.sabordelhimalaya.controller;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;

import com.example.sabordelhimalaya.R;
import com.example.sabordelhimalaya.model.CartRepository;
import com.example.sabordelhimalaya.model.Categoria;
import com.example.sabordelhimalaya.model.Menu;
import com.example.sabordelhimalaya.model.Plato;
import com.example.sabordelhimalaya.model.dto.CategoryResponse;
import com.example.sabordelhimalaya.model.dto.DishResponse;
import com.example.sabordelhimalaya.model.dto.TodayMenuResponse;
import com.example.sabordelhimalaya.util.ApiErrors;
import com.example.sabordelhimalaya.view.Principal2View;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Principal2 extends BaseActivity implements Principal2View.Listener {
    private Principal2View view;
    private CartRepository cart;
    private boolean cargando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_principal2);
        cart = CartRepository.get(this);
        view = new Principal2View(findViewById(R.id.principal_content), this);
        view.initialize(
                () -> startActivity(new Intent(this, Cesta.class)),
                () -> startActivity(new Intent(this, Pedidos.class)),
                () -> startActivity(new Intent(this, Perfil.class)),
                () -> startActivity(new Intent(this, Menus.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ensureCliente()) {
            return;
        }
        cargarCarta();
    }

    private void cargarCarta() {
        if (cargando) {
            return;
        }
        cargando = true;
        view.showLoading();

        final int[] pendientes = {2};
        Runnable terminado = () -> {
            if (--pendientes[0] <= 0) {
                cargando = false;
            }
        };

        api().categories().enqueue(new Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(Call<List<CategoryResponse>> call,
                                   Response<List<CategoryResponse>> response) {
                if (isFinishing()) {
                    return;
                }
                if (handle401(response)) {
                    terminado.run();
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    view.showError(ApiErrors.message(response), Principal2.this::cargarCarta);
                    terminado.run();
                    return;
                }
                List<Categoria> categorias = new ArrayList<>();
                for (CategoryResponse item : response.body()) {
                    categorias.add(Categoria.from(item));
                }
                view.showCategorias(categorias);
                pendientes[0] += categorias.size();
                terminado.run();

                for (Categoria categoria : categorias) {
                    cargarPlatos(categoria.getNumero(), terminado);
                }
            }

            @Override
            public void onFailure(Call<List<CategoryResponse>> call, Throwable t) {
                if (isFinishing()) {
                    return;
                }
                view.showError(ApiErrors.networkMessage(t), Principal2.this::cargarCarta);
                terminado.run();
            }
        });

        api().todayMenu().enqueue(new Callback<TodayMenuResponse>() {
            @Override
            public void onResponse(Call<TodayMenuResponse> call, Response<TodayMenuResponse> response) {
                if (isFinishing()) {
                    return;
                }
                if (handle401(response)) {
                    terminado.run();
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    view.showMenuMessage(ApiErrors.message(response));
                } else if (!response.body().disponible || response.body().menu == null) {
                    view.showMenuMessage(response.body().mensaje != null
                            ? response.body().mensaje
                            : getString(R.string.carta_no_menu));
                } else {
                    view.showMenu(Menu.from(response.body().menu));
                }
                terminado.run();
            }

            @Override
            public void onFailure(Call<TodayMenuResponse> call, Throwable t) {
                if (isFinishing()) {
                    return;
                }
                view.showMenuMessage(ApiErrors.networkMessage(t));
                terminado.run();
            }
        });
    }

    private void cargarPlatos(int numero, Runnable terminado) {
        api().dishes(numero).enqueue(new Callback<List<DishResponse>>() {
            @Override
            public void onResponse(Call<List<DishResponse>> call, Response<List<DishResponse>> response) {
                if (isFinishing()) {
                    terminado.run();
                    return;
                }
                if (handle401(response)) {
                    terminado.run();
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    view.showPlatosError(numero, ApiErrors.message(response));
                } else {
                    List<Plato> platos = new ArrayList<>();
                    for (DishResponse dish : response.body()) {
                        platos.add(Plato.from(dish));
                    }
                    view.showPlatos(numero, platos);
                }
                terminado.run();
            }

            @Override
            public void onFailure(Call<List<DishResponse>> call, Throwable t) {
                if (!isFinishing()) {
                    view.showPlatosError(numero, ApiErrors.networkMessage(t));
                }
                terminado.run();
            }
        });
    }

    @Override
    public void onAddPlato(Plato plato) {
        cart.addPlato(plato);
        showMessage(getString(R.string.carta_added, plato.getNombre()));
    }

    @Override
    public void onPlatoClick(Plato plato) {
        Intent intent = new Intent(this, PlatoDetalle.class);
        intent.putExtra(PlatoDetalle.EXTRA_ID, plato.getId());
        intent.putExtra(PlatoDetalle.EXTRA_NOMBRE, plato.getNombre());
        intent.putExtra(PlatoDetalle.EXTRA_DESCRIPCION, plato.getDescripcion());
        intent.putExtra(PlatoDetalle.EXTRA_IMAGEN, plato.getImagenUrl());
        intent.putExtra(PlatoDetalle.EXTRA_PRECIO,
                plato.getPrecio() == null ? null : plato.getPrecio().toPlainString());
        intent.putExtra(PlatoDetalle.EXTRA_CATEGORIA, plato.getCategoriaNombre());
        startActivity(intent);
    }

    @Override
    public void onMenuClick(Menu menu) {
        Intent intent = new Intent(this, MenuDetalle.class);
        intent.putExtra(MenuDetalle.EXTRA_MENU_ID, menu.getId());
        intent.putExtra(MenuDetalle.EXTRA_MENU_NAME, menu.getNombre());
        startActivity(intent);
    }
}
