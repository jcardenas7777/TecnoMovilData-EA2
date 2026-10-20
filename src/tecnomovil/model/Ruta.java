package tecnomovil.model;

import java.util.List;

public record Ruta(String id, String nombre, List<String> estaciones, int peso) {
public Ruta {
// Copia defensiva: aunque nos pasen una lista modificable, guardamos una inmutable.
estaciones = List.copyOf(estaciones);
}
}
