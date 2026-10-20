package tecnomovil;

import java.time.LocalDateTime;
import tecnomovil.model.Accion;
import tecnomovil.model.Registro;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import tecnomovil.analytics.AnalizadorTransporte;
import tecnomovil.data.Catalogo;
import tecnomovil.data.GeneradorDatos;


public class Main {

public static void main(String[] args) {
Registro r = new Registro(1, "T1", "Central", Accion.ENTRADA, LocalDateTime.of(2026, 3, 16, 7, 0));
System.out.println(r);
System.out.println("Estación: " + r.estacion());
ejecutarAnalisisDePrueba();
ejecutarAnalisisCompleto();

}


// Atajo para crear registros del día 16/03/2026 con una hora "HH:mm:ss"
private static Registro reg(int u, String ruta, String est, Accion a, String hora) {
return new Registro(u, ruta, est, a, LocalDateTime.parse("2026-03-16T" + hora));
}
// Mini conjunto de datos, ordenado por hora (los mismos 8 registros de la guía de estudio)
private static List<Registro> datosDePrueba() {
return List.of(
reg(1, "T1", "A", Accion.ENTRADA, "07:00:00"),
reg(1, "T1", "B", Accion.SALIDA, "07:10:00"),
reg(2, "T1", "A", Accion.ENTRADA, "07:30:00"),
reg(3, "T1", "A", Accion.ENTRADA, "07:45:00"),
reg(2, "T1", "C", Accion.SALIDA, "07:50:00"),
reg(3, "T1", "B", Accion.SALIDA, "07:55:00"),
reg(1, "A1", "B", Accion.ENTRADA, "17:00:00"),
reg(1, "A1", "C", Accion.SALIDA, "17:06:00")).stream()
.sorted(Comparator.comparing(Registro::timestamp))
.toList();
}

private static void ejecutarAnalisisDePrueba() {

List<Registro> registros = datosDePrueba();
AnalizadorTransporte analizador = new AnalizadorTransporte(false);
System.out.println("Tarea 1 - Afluencia por estación: " + analizador.afluenciaPorEstacion(registros));

System.out.println("Tarea 2 - Horas pico: " + analizador.horasPico(registros, 3));
System.out.println("Tarea 3 - Rutas más usadas: " + analizador.rutasMasUtilizadas(registros, 3));

System.out.println("Tarea 4 - Patrones de viaje: " + analizador.patronesDeViaje(registros));

System.out.println("Tarea 5 - Tiempo promedio (min): " + analizador.tiempoPromedioEntreEstaciones(registros));

Map<String, Integer> capacidades = Map.of("T1", 2, "A1", 2); // capacidades inventadas para la prueba
System.out.println("Tarea 6 - Sobrecarga:");
analizador.detectarSobrecarga(registros, capacidades, AnalizadorTransporte.superaUmbral(0.8))
.forEach(System.out::println);
System.out.println("Extra - Afluencia 7:00 a 8:59: " + analizador.afluenciaPorEstacionEnFranja(registros, 7, 8));
}






private static void ejecutarAnalisisCompleto() {
// 1. Datos simulados (misma semilla = mismos datos siempre)
List<Registro> registros = GeneradorDatos.generar(120_000, 2026L);
System.out.printf("Registros generados: %,d%n%n", registros.size());
AnalizadorTransporte analizador = new AnalizadorTransporte(false);


// 2. Las seis tareas
System.out.println("--- 1) Afluencia por estación (top 5) ---");
AnalizadorTransporte.topN(analizador.afluenciaPorEstacion(registros), 5)

.forEach(e -> System.out.printf(" %-22s %,d entradas%n", e.getKey(), e.getValue()));
System.out.println("--- 2) Horas pico (top 5) ---");
analizador.horasPico(registros, 5)
.forEach(e -> System.out.printf(" %02d:00 h %,d entradas%n", e.getKey(), e.getValue()));
System.out.println("--- 3) Rutas más utilizadas (top 5) ---");
analizador.rutasMasUtilizadas(registros, 5)
.forEach(e -> System.out.printf(" %-4s %,d abordajes%n", e.getKey(), e.getValue()));
System.out.println("--- 4) Patrones de viaje (usuarios 1 a 3) ---");
Map<Integer, List<String>> patrones = analizador.patronesDeViaje(registros);
List.of(1, 2, 3).forEach(id -> System.out.println(" U-" + id + ": " + String.join(" → ", patrones.get(id))));
System.out.println("--- 5) Tiempo promedio entre estaciones (5 más lentos) ---");
AnalizadorTransporte.topN(analizador.tiempoPromedioEntreEstaciones(registros), 5)
.forEach(e -> System.out.printf(" %-36s %.1f min%n", e.getKey(), e.getValue()));
System.out.println("--- 6) Sobrecarga (crítica si > 90 %) ---");
analizador.detectarSobrecarga(registros, Catalogo.CAPACIDAD_DIARIA, AnalizadorTransporte.superaUmbral(0.90))
.forEach(e -> System.out.printf(" %-4s %,7d / %,7d = %5.1f %% %s%n",
e.ruta(), e.usos(), e.capacidad(), e.ocupacion() * 100, e.nivel()));


// 3. Inmutabilidad: intentar modificar la lista original debe fallar
try {
registros.add(registros.get(0));
System.out.println("¡MAL! La lista se pudo modificar");
} catch (UnsupportedOperationException e) {
System.out.println("Lista original protegida: UnsupportedOperationException (correcto)");
}
// 4. Paralelo: el mismo análisis con parallelStream() debe dar EXACTAMENTE lo mismo
AnalizadorTransporte paralelo = new AnalizadorTransporte(true);
boolean igual = analizador.afluenciaPorEstacion(registros).equals(paralelo.afluenciaPorEstacion(registros))
&& analizador.patronesDeViaje(registros).equals(paralelo.patronesDeViaje(registros))
&& analizador.tiempoPromedioEntreEstaciones(registros)
.equals(paralelo.tiempoPromedioEntreEstaciones(registros));
System.out.println("Resultado paralelo idéntico al secuencial: " + igual);


}


}
