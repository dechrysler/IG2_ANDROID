package com.example.sabordelhimalaya.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MenuDiarioRepository {
    public MenuDiario cargar(InputStream archivo, List<Plato> carta) throws IOException {
        Map<String, Plato> porNombre = new HashMap<>();
        List<Plato> bebidas = new ArrayList<>();
        for (Plato plato : carta) {
            porNombre.put(plato.getNombre(), plato);
            if (plato.getCategoria() == Categoria.BEBIDAS) bebidas.add(plato);
        }

        List<Plato> entrantes = new ArrayList<>();
        List<Plato> principales = new ArrayList<>();
        List<Plato> postres = new ArrayList<>();
        Map<String, List<Plato>> grupos = Map.of(
                "ENTRANTE", entrantes, "PRINCIPAL", principales, "POSTRE", postres);
        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(archivo, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] datos = linea.split("\\|", -1);
                if (datos.length != 2) throw new IOException("Formato de menú incorrecto");
                List<Plato> grupo = grupos.get(datos[0].trim());
                Plato plato = porNombre.get(datos[1].trim());
                if (grupo == null || plato == null || grupo.contains(plato)) {
                    throw new IOException("Opción de menú incorrecta");
                }
                grupo.add(plato);
            }
        }
        if (entrantes.size() != 3 || principales.size() != 3 || postres.size() != 2
                || bebidas.isEmpty()) {
            throw new IOException("Menú diario incompleto");
        }
        return new MenuDiario(LocalDate.now(), entrantes, principales, postres, bebidas, 15.0);
    }
}
