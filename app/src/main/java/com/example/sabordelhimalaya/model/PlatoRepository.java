package com.example.sabordelhimalaya.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class PlatoRepository {
    public List<Plato> cargar(InputStream archivo) throws IOException {
        List<Plato> platos = new ArrayList<>();
        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(archivo, StandardCharsets.UTF_8))) {
            String linea;
            int numeroLinea = 0;
            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (linea.trim().isEmpty()) continue;
                String[] datos = linea.split("\\|", -1);
                if (datos.length != 4) {
                    throw new IOException("Formato de plato incorrecto en la línea " + numeroLinea);
                }
                try {
                    platos.add(new Plato(datos[0].trim(), Categoria.valueOf(datos[1].trim()),
                            datos[2].trim(), Double.parseDouble(datos[3].trim())));
                } catch (IllegalArgumentException exception) {
                    throw new IOException("Datos de plato incorrectos en la línea " + numeroLinea,
                            exception);
                }
            }
        }
        return platos;
    }
}
