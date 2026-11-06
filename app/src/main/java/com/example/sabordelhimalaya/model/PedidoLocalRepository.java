package com.example.sabordelhimalaya.model;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class PedidoLocalRepository {
    public String guardar(File almacenamiento, HacerPedido pedido) throws IOException, JSONException {
        JSONArray items = new JSONArray();
        for (Object item : pedido.getItems()) {
            items.put(item instanceof Plato ? platoJson((Plato) item) : menuJson((MenuDiario) item));
        }
        JSONObject datos = new JSONObject()
                .put("idUsuario", pedido.getIdUsuario())
                .put("fecha", pedido.getFecha().toString())
                .put("items", items)
                .put("numeroTotalItems", pedido.getNumeroTotalItems())
                .put("precioTotal", pedido.getPrecioTotal());
        String json = datos.toString(2);

        Path carpeta = Files.createDirectories(almacenamiento.toPath().resolve("pedidos"));
        Path temporal = Files.createTempFile(carpeta, "pedido-", ".tmp");
        try {
            Files.write(temporal, json.getBytes(StandardCharsets.UTF_8));
            Files.move(temporal, carpeta.resolve("pedido-" + UUID.randomUUID() + ".json"),
                    StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temporal);
        }
        return json;
    }

    private JSONObject platoJson(Plato plato) throws JSONException {
        return new JSONObject().put("tipo", "PLATO")
                .put("nombre", plato.getNombre())
                .put("categoria", plato.getCategoria().name())
                .put("descripcion", plato.getDescripcion())
                .put("precio", plato.getPrecio());
    }

    public List<Pedido> cargar(File almacenamiento) throws IOException, JSONException {
        File carpeta = new File(almacenamiento, "pedidos");
        List<Pedido> pedidos = new ArrayList<>();
        if (!carpeta.exists()) return pedidos;
        File[] archivos = carpeta.listFiles((dir, nombre) -> nombre.endsWith(".json"));
        if (archivos == null) throw new IOException("No se puede leer la carpeta de pedidos");
        Arrays.sort(archivos, Comparator.comparing(File::getName));
        List<JSONObject> documentos = new ArrayList<>();
        for (File archivo : archivos) {
            documentos.add(new JSONObject(new String(Files.readAllBytes(archivo.toPath()),
                    StandardCharsets.UTF_8)));
        }
        documentos.sort(Comparator.comparing(datos -> datos.optString("fecha")));
        for (JSONObject datos : documentos) {
            List<Plato> platos = new ArrayList<>();
            List<MenuDiario> menus = new ArrayList<>();
            JSONArray items = datos.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                switch (item.getString("tipo")) {
                    case "PLATO":
                        platos.add(leerPlato(item));
                        break;
                    case "MENU_DIARIO":
                        menus.add(new MenuDiario(LocalDate.parse(item.getString("fecha")),
                                leerPlatos(item.getJSONArray("entrantes")),
                                leerPlatos(item.getJSONArray("principales")),
                                leerPlatos(item.getJSONArray("postres")),
                                leerPlatos(item.getJSONArray("bebidas")), item.getDouble("precio")));
                        break;
                    default:
                        throw new JSONException("Tipo de artículo desconocido");
                }
            }
            Pedido pedido = new Pedido(pedidos.size() + 1, LocalDateTime.parse(datos.getString("fecha")),
                    platos, menus, datos.getInt("numeroTotalItems"), datos.getDouble("precioTotal"),
                    EstadoPedidos.RECIBIDO);
            pedido.setIdUsuario(datos.getInt("idUsuario"));
            pedidos.add(pedido);
        }
        return pedidos;
    }

    private Plato leerPlato(JSONObject datos) throws JSONException {
        return new Plato(datos.getString("nombre"), Categoria.valueOf(datos.getString("categoria")),
                datos.getString("descripcion"), datos.getDouble("precio"));
    }

    private List<Plato> leerPlatos(JSONArray datos) throws JSONException {
        List<Plato> platos = new ArrayList<>();
        for (int i = 0; i < datos.length(); i++) platos.add(leerPlato(datos.getJSONObject(i)));
        return platos;
    }

    private JSONObject menuJson(MenuDiario menu) throws JSONException {
        return new JSONObject().put("tipo", "MENU_DIARIO")
                .put("fecha", menu.getFecha().toString())
                .put("entrantes", platosJson(menu.getEntrantes()))
                .put("principales", platosJson(menu.getPrincipales()))
                .put("postres", platosJson(menu.getPostres()))
                .put("bebidas", platosJson(menu.getBebidas()))
                .put("precio", menu.getPrecio());
    }

    private JSONArray platosJson(List<Plato> platos) throws JSONException {
        JSONArray resultado = new JSONArray();
        for (Plato plato : platos) resultado.put(platoJson(plato));
        return resultado;
    }
}
