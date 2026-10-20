package tecnomovil.data;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import tecnomovil.model.Ruta;
public final class Catalogo {
private Catalogo() {
}
public static final List<Ruta> RUTAS = List.of(
new Ruta("T1", "Troncal Norte-Sur",
List.of("Terminal Norte", "La Aurora", "Parque Tecnológico", "Plaza Mayor", "Central",
"Hospital Regional", "Universidad", "Mercado Sur", "Terminal Sur"), 30),
new Ruta("T2", "Troncal Oriente-Occidente",
List.of("Puerto Seco", "El Mirador", "Biblioteca", "Central", "Estadio", "Las Palmas",
"Terminal Occidente"), 20),
new Ruta("E1", "Expreso Centro",
List.of("Terminal Norte", "Plaza Mayor", "Central", "Universidad", "Terminal Sur"), 15),
new Ruta("A1", "Alimentadora Norte",
List.of("Barrio Esperanza", "Villa Verde", "La Aurora", "Parque Tecnológico"), 12),
new Ruta("A2", "Alimentadora Sur",

List.of("Mercado Sur", "San José", "Los Almendros", "Terminal Sur"), 10),
new Ruta("A3", "Alimentadora Oriente",
List.of("Ciudad Jardín", "Los Pinos", "Biblioteca", "Central"), 13));
public static final Map<String, Integer> CAPACIDAD_DIARIA = Map.of(
"T1", 100_000,
"T2", 70_000,
"E1", 55_000,
"A1", 45_000,
"A2", 40_000,
"A3", 55_000);
public static final List<Ruta> RUTAS_PONDERADAS = RUTAS.stream()
.flatMap(r -> Collections.nCopies(r.peso(), r).stream())
.toList();
}