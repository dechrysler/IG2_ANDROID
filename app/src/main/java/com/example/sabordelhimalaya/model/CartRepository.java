package com.example.sabordelhimalaya.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.sabordelhimalaya.model.dto.CreateOrderRequest;
import com.example.sabordelhimalaya.model.dto.OrderItemRequest;
import com.example.sabordelhimalaya.model.remote.ApiClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cesta local del cliente. Vive en memoria y se persiste en SharedPreferences
 * privado para sobrevivir a cambios de pantalla y reinicios suaves.
 */
public final class CartRepository {
    private static final String PREFS = "sabor_cart";
    private static final String KEY_ITEMS = "items";

    private static CartRepository instance;

    private final SharedPreferences preferences;
    private final List<CartItem> items = new ArrayList<>();
    private String editingOrderId;
    private String editingAddress;

    private CartRepository(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        load();
    }

    public static synchronized CartRepository get(Context context) {
        if (instance == null) {
            instance = new CartRepository(context);
        }
        return instance;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int totalUnidades() {
        int total = 0;
        for (CartItem item : items) {
            total += item.cantidad;
        }
        return total;
    }

    public BigDecimal subtotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            if (item.precio != null) {
                total = total.add(item.precio.multiply(BigDecimal.valueOf(item.cantidad)));
            }
        }
        return total;
    }

    /** Cestas antiguas pueden contener menús sin las cuatro opciones. */
    public boolean tieneMenusSinSeleccion() {
        for (CartItem item : items) {
            if (item.esMenu() && !item.tieneSeleccionCompleta()) {
                return true;
            }
        }
        return false;
    }

    public void addPlato(Plato plato) {
        addPlato(plato, 1);
    }

    public void addPlato(Plato plato, int cantidad) {
        CartItem existing = find("PRODUCTO", plato.getId());
        if (existing != null) {
            existing.cantidad += cantidad;
        } else {
            items.add(new CartItem("PRODUCTO", plato.getId(), plato.getNombre(),
                    plato.getPrecio(), cantidad, List.of(), plato.getImagenUrl()));
        }
        save();
    }

    public void addMenu(Menu menu, SeleccionMenu seleccion, int cantidad) {
        if (seleccion == null || !seleccion.estaCompleta()) {
            return;
        }
        CartItem existing = findMenu(menu.getId(), seleccion);
        if (existing != null) {
            existing.cantidad += cantidad;
        } else {
            CartItem item = new CartItem("MENU", menu.getId(), menu.getNombre(),
                    menu.getPrecio(), cantidad, nombresSeleccion(seleccion), null);
            item.aplicarSeleccion(seleccion);
            items.add(item);
        }
        save();
    }

    public void increase(int index) {
        if (index < 0 || index >= items.size()) {
            return;
        }
        items.get(index).cantidad++;
        save();
    }

    public void decrease(int index) {
        if (index < 0 || index >= items.size()) {
            return;
        }
        CartItem item = items.get(index);
        if (item.cantidad <= 1) {
            items.remove(index);
        } else {
            item.cantidad--;
        }
        save();
    }

    public void remove(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            save();
        }
    }

    public void clear() {
        items.clear();
        editingOrderId = null;
        editingAddress = null;
        save();
    }

    /** Carga un pedido existente para la pantalla de modificación (PUT). */
    public void loadForEdit(Pedido pedido) {
        items.clear();
        for (LineaPedido linea : pedido.getItems()) {
            if (linea.getPlatoId() != null) {
                items.add(new CartItem("PRODUCTO", linea.getPlatoId(), linea.getNombre(),
                        linea.getPrecioUnitario(), linea.getCantidad(), List.of(), null));
            } else if (linea.getMenuId() != null) {
                CartItem item = new CartItem("MENU", linea.getMenuId(), linea.getNombre(),
                        linea.getPrecioUnitario(), linea.getCantidad(),
                        linea.getComponentes(), null);
                if (linea.getSeleccion() != null && linea.getSeleccion().estaCompleta()) {
                    item.aplicarSeleccion(linea.getSeleccion());
                }
                items.add(item);
            }
        }
        editingOrderId = pedido.getId();
        editingAddress = pedido.getDireccionEntrega();
        save();
    }

    public String getEditingOrderId() {
        return editingOrderId;
    }

    public String getEditingAddress() {
        return editingAddress;
    }

    public void finishEdit() {
        editingOrderId = null;
        editingAddress = null;
        save();
    }

    public CreateOrderRequest toRequest(String direccion) {
        List<OrderItemRequest> lineas = new ArrayList<>();
        for (CartItem item : items) {
            if ("MENU".equals(item.tipo)) {
                lineas.add(OrderItemRequest.menu(item.id,
                        item.entranteId, item.principalId,
                        item.postreId, item.bebidaId, item.cantidad));
            } else {
                lineas.add(OrderItemRequest.producto(item.id, item.cantidad));
            }
        }
        return new CreateOrderRequest(lineas, direccion);
    }

    private List<String> nombresSeleccion(SeleccionMenu seleccion) {
        List<String> nombres = new ArrayList<>();
        nombres.add(nombre(seleccion.getEntrante()));
        nombres.add(nombre(seleccion.getPrincipal()));
        nombres.add(nombre(seleccion.getPostre()));
        nombres.add(nombre(seleccion.getBebida()));
        return nombres;
    }

    private String nombre(OpcionSeleccionada opcion) {
        return opcion != null && opcion.getNombre() != null ? opcion.getNombre() : "";
    }

    private CartItem find(String tipo, String id) {
        for (CartItem item : items) {
            if (item.tipo.equals(tipo) && item.id.equals(id)) {
                return item;
            }
        }
        return null;
    }

    private CartItem findMenu(String menuId, SeleccionMenu seleccion) {
        for (CartItem item : items) {
            if (item.esMenu()
                    && menuId.equals(item.id)
                    && item.coincideSeleccion(seleccion)) {
                return item;
            }
        }
        return null;
    }

    private void save() {
        preferences.edit()
                .putString(KEY_ITEMS, ApiClient.gson().toJson(items))
                .apply();
    }

    private void load() {
        String json = preferences.getString(KEY_ITEMS, null);
        if (json == null || json.isEmpty()) {
            return;
        }
        try {
            CartItem[] stored = ApiClient.gson().fromJson(json, CartItem[].class);
            if (stored != null) {
                Collections.addAll(items, stored);
            }
        } catch (RuntimeException ignored) {
            items.clear();
        }
    }

    /** Línea de la cesta. Campos mutables para que Gson los persista. */
    public static class CartItem {
        public String tipo;
        public String id;
        public String nombre;
        public BigDecimal precio;
        public int cantidad;
        public List<String> componentes;
        public String imagenUrl;

        public String entranteId;
        public String entranteNombre;
        public String principalId;
        public String principalNombre;
        public String postreId;
        public String postreNombre;
        public String bebidaId;
        public String bebidaNombre;

        public CartItem() {
        }

        public CartItem(String tipo, String id, String nombre, BigDecimal precio,
                        int cantidad, List<String> componentes, String imagenUrl) {
            this.tipo = tipo;
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
            this.cantidad = cantidad;
            this.componentes = componentes;
            this.imagenUrl = imagenUrl;
        }

        public boolean esMenu() {
            return "MENU".equals(tipo);
        }

        public boolean tieneSeleccionCompleta() {
            return entranteId != null && principalId != null
                    && postreId != null && bebidaId != null;
        }

        public boolean coincideSeleccion(SeleccionMenu seleccion) {
            if (seleccion == null) {
                return false;
            }
            return equals(opcionId(seleccion.getEntrante()), entranteId)
                    && equals(opcionId(seleccion.getPrincipal()), principalId)
                    && equals(opcionId(seleccion.getPostre()), postreId)
                    && equals(opcionId(seleccion.getBebida()), bebidaId);
        }

        public void aplicarSeleccion(SeleccionMenu seleccion) {
            if (seleccion == null) {
                return;
            }
            entranteId = opcionId(seleccion.getEntrante());
            entranteNombre = opcionNombre(seleccion.getEntrante());
            principalId = opcionId(seleccion.getPrincipal());
            principalNombre = opcionNombre(seleccion.getPrincipal());
            postreId = opcionId(seleccion.getPostre());
            postreNombre = opcionNombre(seleccion.getPostre());
            bebidaId = opcionId(seleccion.getBebida());
            bebidaNombre = opcionNombre(seleccion.getBebida());
        }

        public SeleccionMenu getSeleccion() {
            return SeleccionMenu.from(
                    new OpcionSeleccionada(entranteId, entranteNombre),
                    new OpcionSeleccionada(principalId, principalNombre),
                    new OpcionSeleccionada(postreId, postreNombre),
                    new OpcionSeleccionada(bebidaId, bebidaNombre)
            );
        }

        private static String opcionId(OpcionSeleccionada opcion) {
            return opcion != null ? opcion.getId() : null;
        }

        private static String opcionNombre(OpcionSeleccionada opcion) {
            return opcion != null ? opcion.getNombre() : null;
        }

        private static boolean equals(String a, String b) {
            return a == null ? b == null : a.equals(b);
        }
    }
}
